@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*
import icu.windea.pls.util.*
import org.jetbrains.annotations.*
import kotlin.Pair

//Misc Extensions

fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getSettings() = ParadoxSettingsState.getInstance()

fun getConfig(): CwtConfigGroupsCache {
	return ServiceManager.getService(getDefaultProject(), CwtConfigGroupProvider::class.java).configGroupsCache
}

fun getConfig(project: Project) = ServiceManager.getService(project, CwtConfigGroupProvider::class.java).configGroupsCache

fun inferParadoxLocale() = when(System.getProperty("user.language")) {
	"zh" -> getConfig().localeMap.getValue("l_simp_chinese")
	"en" -> getConfig().localeMap.getValue("l_english")
	"pt" -> getConfig().localeMap.getValue("l_braz_por")
	"fr" -> getConfig().localeMap.getValue("l_french")
	"de" -> getConfig().localeMap.getValue("l_german")
	"pl" -> getConfig().localeMap.getValue("l_ponish")
	"ru" -> getConfig().localeMap.getValue("l_russian")
	"es" -> getConfig().localeMap.getValue("l_spanish")
	else -> getConfig().localeMap.getValue("l_english")
}

/**得到指定元素之前的所有直接的注释的文本，作为文档注释，跳过空白。*/
fun getDocTextFromPreviousComment(element: PsiElement): String {
	//我们认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释，但这并非文档注释的全部
	val lines = mutableListOf<String>()
	var prevElement = element.prevSibling ?: element.parent?.prevSibling
	while(prevElement != null) {
		val text = prevElement.text
		if(prevElement !is PsiWhiteSpace) {
			if(!isPreviousComment(prevElement)) break
			val documentText = text.trimStart('#').trim().escapeXml()
			lines.add(0, documentText)
		} else {
			if(text.containsBlankLine()) break
		}
		// 兼容comment在rootBlock之外的特殊情况
		prevElement = prevElement.prevSibling
	}
	return lines.joinToString("<br>")
}

/**判断指定的注释是否可认为是之前的注释。*/
fun isPreviousComment(element: PsiElement): Boolean {
	val elementType = element.elementType
	return elementType == ParadoxLocalisationTypes.COMMENT || elementType == COMMENT
}

//Keys

val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
val cachedParadoxFileInfoKey = Key<CachedValue<ParadoxFileInfo>>("cachedParadoxFileInfo")
val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")

//PsiElement Extensions

val ParadoxLocalisationLocale.paradoxLocale: ParadoxLocale?
	get() {
		val name = this.name
		return getConfig().localeMap[name]
	}

val ParadoxLocalisationPropertyReference.paradoxColor: ParadoxColor?
	get() {
		val colorId = this.propertyReferenceParameter?.text?.firstOrNull()
		if(colorId != null && colorId.isUpperCase()) {
			return getConfig().colorMap[colorId.toString()]
		}
		return null
	}

val ParadoxLocalisationSequentialNumber.paradoxSequentialNumber: ParadoxSequentialNumber?
	get() {
		val name = this.name
		return getConfig().sequentialNumberMap[name]
	}

val ParadoxLocalisationColorfulText.paradoxColor: ParadoxColor?
	get() {
		val name = this.name
		return getConfig().colorMap[name]
	}

//val ParadoxLocalisationCommandScope.paradoxCommandScope: ParadoxCommandScope?
//	get() {
//		val name = this.name.toCapitalizedWord() //忽略大小写，首字母大写
//		if(name.startsWith(eventTargetPrefix)) return null
//		return config.commandScopeMap[name]
//	}
//
//val ParadoxLocalisationCommandField.paradoxCommandField: ParadoxCommandField?
//	get() {
//		val name = this.name
//		return config.commandFieldMap[name]
//	}

val PsiElement.paradoxLocale: ParadoxLocale? get() = getLocale(this)

private fun getLocale(element: PsiElement): ParadoxLocale? {
	return when(val file = element.containingFile) {
		is ParadoxScriptFile -> inferParadoxLocale()
		is ParadoxLocalisationFile -> file.locale?.paradoxLocale
		else -> null
	}
}

val VirtualFile.paradoxFileInfo: ParadoxFileInfo? get() = this.getUserData(paradoxFileInfoKey)

val PsiFile.paradoxFileInfo: ParadoxFileInfo? get() = getFileInfo(this.originalFile) //使用原始文件

val PsiElement.paradoxFileInfo: ParadoxFileInfo? get() = getFileInfo(this.containingFile)

internal fun canGetFileInfo(file: PsiFile): Boolean {
	//paradoxScriptFile, paradoxLocalisationFile, ddsFile
	if(file is ParadoxScriptFile || file is ParadoxLocalisationFile) return true
	val extension = file.name.substringAfterLast('.').lowercase()
	if(extension == "dds") return true
	return false
}

private fun getFileInfo(file: PsiFile): ParadoxFileInfo? {
	if(!canGetFileInfo(file)) return null
	//尝试基于fileViewProvider得到fileInfo
	val quickFileInfo = file.getUserData(paradoxFileInfoKey)
	if(quickFileInfo != null) return quickFileInfo
	return CachedValuesManager.getCachedValue(file, cachedParadoxFileInfoKey) {
		val value = file.virtualFile?.getUserData(paradoxFileInfoKey) ?: resolveFileInfo(file)
		CachedValueProvider.Result.create(value, file)
	}
}

private fun resolveFileInfo(file: PsiFile): ParadoxFileInfo? {
	val fileType = getFileType(file) ?: return null
	val fileName = file.name
	val subpaths = mutableListOf(fileName)
	var currentFile = file.parent
	while(currentFile != null) {
		val rootType = getRootType(currentFile)
		val rootPath = currentFile.virtualFile.toNioPath()
		if(rootType != null) {
			val path = getPath(subpaths)
			val gameType = getGameType(currentFile) ?: ParadoxGameType.defaultValue()
			return ParadoxFileInfo(fileName, path, rootPath, fileType, rootType, gameType)
		}
		subpaths.add(0, currentFile.name)
		currentFile = currentFile.parent
	}
	return null
}

private fun getPath(subpaths: List<String>): ParadoxPath {
	return ParadoxPath(subpaths)
}

private fun getFileType(file: PsiFile): ParadoxFileType? {
	val fileName = file.name.lowercase()
	val fileExtension = fileName.substringAfterLast('.')
	return when {
		fileExtension in scriptFileExtensions -> ParadoxFileType.ParadoxScript
		fileExtension in localisationFileExtensions -> ParadoxFileType.ParadoxLocalisation
		else -> null
	}
}

private fun getRootType(file: PsiDirectory): ParadoxRootType? {
	if(!file.isDirectory) return null
	val fileName = file.name.lowercase()
	for(child in file.files) {
		val childName = child.name.lowercase()
		val childExpression = childName.substringAfterLast('.', "")
		when {
			//TODO 可能并不是这样命名，需要检查
			//childName in ParadoxGameType.exeFileNames -> return ParadoxRootType.Stdlib
			childExpression == "exe" -> return ParadoxRootType.Stdlib
			childName == descriptorFileName -> return ParadoxRootType.Mod
			fileName == ParadoxRootType.PdxLauncher.key -> return ParadoxRootType.PdxLauncher
			fileName == ParadoxRootType.PdxOnlineAssets.key -> return ParadoxRootType.PdxOnlineAssets
			fileName == ParadoxRootType.TweakerGuiAssets.key -> return ParadoxRootType.TweakerGuiAssets
		}
	}
	return null
}

private fun getGameType(file: PsiDirectory): ParadoxGameType? {
	if(!file.isDirectory) return null
	for(child in file.files) {
		val childName = child.name
		if(childName.startsWith('.')) {
			val gameType = ParadoxGameType.resolve(childName.drop(1))
			if(gameType != null) return gameType
		}
	}
	return null
}

val ParadoxScriptProperty.paradoxDefinitionInfo: ParadoxDefinitionInfo? get() = inferDefinitionInfo(this)

private fun inferDefinitionInfo(element: ParadoxScriptProperty): ParadoxDefinitionInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionInfoKey) {
		CachedValueProvider.Result.create(resolveDefinitionInfo(element), element)
	}
}

private fun resolveDefinitionInfo(element: ParadoxScriptProperty): ParadoxDefinitionInfo? {
	//NOTE cwt文件中定义的definition的minDepth是4（跳过3个rootKey）
	val propertyPath = element.resolvePropertyPath(4) ?: return null
	val fileInfo = element.paradoxFileInfo ?: return null
	val path = fileInfo.path
	val gameType = fileInfo.gameType
	val elementName = element.name
	val project = element.project
	val configGroup = getConfig(project)[gameType] //这里需要指定project
	return configGroup.inferDefinitionInfo(element, elementName, path, propertyPath)
}

fun ParadoxScriptValue.getType(): String? {
	return when(this) {
		is ParadoxScriptBlock -> when {
			this.isEmpty -> "array | object"
			this.isArray -> "array"
			this.isObject -> "object"
			else -> null
		}
		is ParadoxScriptString -> "string"
		is ParadoxScriptBoolean -> "boolean"
		is ParadoxScriptInt -> "int"
		is ParadoxScriptFloat -> "float"
		is ParadoxScriptNumber -> "number"
		is ParadoxScriptColor -> "color"
		is ParadoxScriptCode -> "code"
		else -> null
	}
}

fun ParadoxScriptValue.checkType(type: String): Boolean {
	return when(type) {
		"block", "array | object" -> this is ParadoxScriptBlock
		"array" -> this is ParadoxScriptBlock && isArray
		"object" -> this is ParadoxScriptBlock && isObject
		"string" -> this is ParadoxScriptString
		"boolean" -> this is ParadoxScriptBoolean
		"int" -> this is ParadoxScriptInt
		"float" -> this is ParadoxScriptFloat
		"number" -> this is ParadoxScriptNumber
		"color" -> this is ParadoxScriptColor
		"code" -> this is ParadoxScriptCode
		else -> false
	}
}

fun ParadoxScriptValue.isNullLike(): Boolean {
	return when {
		this is ParadoxScriptBlock -> this.isEmpty || this.isAlwaysYes() //兼容always=yes
		this is ParadoxScriptString -> this.textMatches("")
		this is ParadoxScriptNumber -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptBoolean -> this.textMatches("no")
		else -> false
	}
}

fun ParadoxScriptBlock.isAlwaysYes(): Boolean {
	return this.isObject && this.propertyList.singleOrNull()?.let { it.name == "always" && it.value == "yes" } ?: false
}

fun PsiElement.resolvePath(): ParadoxPath? {
	val subpaths = mutableListOf<String>()
	var current = this
	while(current !is PsiFile) {
		when {
			current is ParadoxScriptProperty -> {
				subpaths.add(0, current.name)
			}
			current is ParadoxScriptValue -> {
				val parent = current.parent ?: break
				if(parent is ParadoxScriptBlock) {
					subpaths.add(0, parent.indexOfChild(current).toString())
				}
				current = parent
			}
		}
		current = current.parent ?: break
	}
	return if(subpaths.isEmpty()) null else ParadoxPath(subpaths)
}

fun PsiElement.resolvePropertyPath(maxDepth: Int = -1): ParadoxPath? {
	val subpaths = mutableListOf<String>()
	var current = this
	var depth = 0
	while(current !is PsiFile && current !is ParadoxScriptRootBlock) {
		when {
			current is ParadoxScriptProperty -> {
				subpaths.add(0, current.name)
				depth++
			}
			//忽略scriptValue
		}
		//如果发现深度超出指定的最大深度，则直接返回null
		if(maxDepth != -1 && maxDepth < depth) return null
		current = current.parent ?: break
	}
	return if(subpaths.isEmpty()) null else ParadoxPath(subpaths)
}

fun PsiElement.resolveDefinitionInfoAndDefinitionPropertyPath(): Pair<ParadoxDefinitionInfo, ParadoxPath>? {
	val subpaths = mutableListOf<String>()
	var current = this
	while(current !is PsiFile) {
		when {
			current is ParadoxScriptProperty -> {
				val definitionInfo = current.paradoxDefinitionInfo
				if(definitionInfo != null) return definitionInfo to ParadoxPath(subpaths)
				subpaths.add(0, current.name)
			}
		}
		current = current.parent ?: break
	}
	return null
}

val ParadoxLocalisationProperty.paradoxLocalisationInfo:ParadoxLocalisationInfo? get() = inferLocalisationInfo(this)

private fun inferLocalisationInfo(element:ParadoxLocalisationProperty):ParadoxLocalisationInfo?{
	return CachedValuesManager.getCachedValue(element, cachedParadoxLocalisationInfoKey) {
		CachedValueProvider.Result.create(resolveLocalisationInfo(element), element)
	}
}

private fun resolveLocalisationInfo(element: ParadoxLocalisationProperty):ParadoxLocalisationInfo?{
	val name = element.name
	val type = ParadoxLocalisationCategory.resolve(element)?:return null
	return ParadoxLocalisationInfo(name,type)
}

/**
 * 判断当前localisation所在的根目录是否是"localisation"或"localisation_synced"
 */
fun ParadoxLocalisationProperty.isInValidDirectory(): Boolean {
	return this.paradoxFileInfo?.path?.root.let { it != null && it == "localisation" || it == "localisation_synced" }
}

/**
 * 判断当前localisation所在的根目录是否是"localisation"
 */
fun ParadoxLocalisationProperty.isLocalisation(): Boolean {
	return this.paradoxFileInfo?.path?.root == "localisation"
}

/**
 * 判断当前localisation所在的根目录是否是"localisation_synced"
 */
fun ParadoxLocalisationProperty.isLocalisationSynced(): Boolean {
	return this.paradoxFileInfo?.path?.root == "localisation_synced"
}

//PsiElement Find Extensions

fun ParadoxScriptProperty.findProperty(propertyName: String, ignoreCase: Boolean = false): ParadoxScriptProperty? {
	val block = propertyValue?.value as? ParadoxScriptBlock ?: return null
	return block.propertyList.find { it.name.equals(propertyName, ignoreCase) }
}

fun ParadoxScriptProperty.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
	val block = propertyValue?.value as? ParadoxScriptBlock ?: return emptyList()
	return block.propertyList.filter { it.name.equals(propertyName, ignoreCase) }
}

fun ParadoxScriptProperty.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
	val block = propertyValue?.value as? ParadoxScriptBlock ?: return null
	return block.valueList.find { it.value.equals(value, ignoreCase) }
}

fun ParadoxScriptProperty.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
	val block = propertyValue?.value as? ParadoxScriptBlock ?: return emptyList()
	return block.valueList.filter { it.value.equals(value, ignoreCase) }
}

fun ParadoxScriptBlock.findProperty(propertyName: String, ignoreCase: Boolean = false): ParadoxScriptProperty? {
	return propertyList.find { it.name.equals(propertyName, ignoreCase) }
}

fun ParadoxScriptBlock.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
	return valueList.find { it.value.equals(value, ignoreCase) }
}

//Find Extensions
/**
 * 根据名字在当前文件中递归查找脚本变量（scriptedVariable）。（不一定定义在顶层）
 */
fun findScriptVariableInFile(name: String, file: PsiFile): ParadoxScriptVariable? {
	if(file !is ParadoxScriptFile) return null
	return file.descendantsOfType<ParadoxScriptVariable>().find { it.name == name }
}

/**
 * 根据名字在当前文件中递归查找所有的脚本变量（scriptedVariable）。（不一定定义在顶层）
 */
fun findScriptVariablesInFile(name: String, file: PsiFile): List<ParadoxScriptVariable> {
	if(file !is ParadoxScriptFile) return emptyList()
	return file.descendantsOfType<ParadoxScriptVariable>().filter { it.name == name }.toList()
}

/**
 * 在当前文件中递归查找所有的脚本变量（scriptedVariable）。（不一定定义在顶层）
 */
fun findScriptVariablesInFile(file: PsiFile): List<ParadoxScriptVariable> {
	//在所在文件中递归查找（不一定定义在顶层）
	if(file !is ParadoxScriptFile) return emptyList()
	return file.descendantsOfType<ParadoxScriptVariable>().toList()
}

/**
 * 基于脚本变量名字索引，根据名字查找脚本变量（scriptedVariable）。
 */
fun findScriptVariable(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptVariable? {
	return ParadoxScriptVariableNameIndex.getOne(name, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于脚本变量名字索引，根据名字查找所有的脚本变量（scriptedVariable）。
 */
fun findScriptVariables(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.getAll(name, project, scope)
}

/**
 * 基于脚本变量名字索引，查找所有的脚本变量（scriptedVariable）。
 */
fun findScriptVariables(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.getAll(project, scope)
}

/**
 * 基于脚本变量名字索引，过滤所有的脚本变量（scriptedVariable）。
 */
fun filterScriptVariables(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	predicate: (String) -> Boolean
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.filter(project, scope, predicate)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找脚本文件的定义（definition）。
 */
fun findDefinition(
	name: String,
	typeExpression: String? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptProperty? {
	return ParadoxDefinitionNameIndex.getOne(name, typeExpression, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitions(
	name: String,
	typeExpression: String? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.getAll(name, typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitions(
	typeExpression: String? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.getAll(typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据类型表达式查找并根据名字过滤所有的脚本文件的定义（definition）。
 */
fun filterDefinitions(
	typeExpression: String? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	predicate: (String) -> Boolean
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.filter(typeExpression, project, scope, predicate)
}

/**
 * 基于定义类型索引，根据名字和类型（不是类型表达式）查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionByType(
	name: String,
	type: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptProperty? {
	return ParadoxDefinitionTypeIndex.getOne(name, type, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于定义类型索引，根据名字和类型（不是类型表达式）查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionsByType(
	name: String,
	type: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.getAll(name, type, project, scope)
}

/**
 * 基于定义类型索引，根据类型（不是类型表达式）查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionsByType(
	type: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.getAll(type, project, scope)
}

/**
 * 基于定义蕾西索引，根据类型（不是类型表达式）查找并根据名字过滤所有的脚本文件的定义（definition）。
 */
fun filterDefinitionsByType(
	type: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	predicate: (String) -> Boolean
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.filter(type, project, scope, predicate)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找本地化（localisation）。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findLocalisation(
	name: String,
	locale: ParadoxLocale?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.getOne(name, locale, project, scope, hasDefault, !getSettings().preferOverridden)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找所有的本地化（localisation）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findLocalisations(
	name: String,
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = true
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.getAll(name, locale, project, scope, hasDefault)
}

/**
 * 基于本地化名字索引，根据语言区域查找所有的本地化（localisation）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findLocalisations(
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.getAll(locale, project, scope, hasDefault)
}

/**
 * 基于本地化名字索引，根据语言区域查找且根据名字过滤所有的本地化（localisation）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun filterLocalisations(
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	predicate: (String) -> Boolean
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.filter(locale, project, scope, hasDefault, predicate)
}

/**
 * 基于本地化名字索引，根据关键字查找所有的本地化（localisation）。
 * * 如果名字包含关键字（不忽略大小写），则放入结果。
 * * 返回的结果有数量限制。
 */
fun findLocalisationsByKeyword(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findByKeyword(keyword, project, scope)
}

/**
 * 基于本地化名字索引，根据一组名字、语言区域查找所有的本地化（localisation）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 * * 如果[keepOrder]为`true`，则根据这组名字排序查询结果。
 */
fun findLocalisationsByNames(
	names: Iterable<String>,
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	keepOrder: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findByNames(names, locale, project, scope, hasDefault, keepOrder)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找同步本地化（localisation_synced）。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findSyncedLocalisation(
	name: String,
	locale: ParadoxLocale?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxSyncedLocalisationNameIndex.getOne(name, locale, project, scope, hasDefault, !getSettings().preferOverridden)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找所有的同步本地化（localisation_synced）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findSyncedLocalisations(
	name: String,
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = true
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.getAll(name, locale, project, scope, hasDefault)
}

/**
 * 基于同步本地化名字索引，根据语言区域查找所有的同步本地化（localisation_synced）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findSyncedLocalisations(
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.getAll(locale, project, scope, hasDefault)
}

/**
 * 基于同步本地化名字索引，根据语言区域查找且根据名字过滤所有的同步本地化（localisation_synced）。
 * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun filterSyncedLocalisations(
	locale: ParadoxLocale? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	predicate: (String) -> Boolean
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.filter(locale, project, scope, hasDefault, predicate)
}

/**
 * 基于同步本地化名字索引，根据关键字查找所有的同步本地化（localisation_synced）。
 * * 如果名字包含关键字（不忽略大小写），则放入结果。
 * * 返回的结果有数量限制。
 */
fun findSyncedLocalisationsByKeyword(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findByKeyword(keyword, project, scope)
}

//Link Extensions

fun resolveLink(link: String, context: PsiElement): PsiElement? {
	return when {
		link.startsWith("$") -> resolveScriptLink(link, context)
		link.startsWith("#") -> resolveLocalisationLink(link, context)
		else -> null
	}
}

private fun resolveScriptLink(link: String, context: PsiElement): ParadoxScriptProperty? {
	return findDefinition(link.drop(1), null, context.project)
}

private fun resolveLocalisationLink(link: String, context: PsiElement): ParadoxLocalisationProperty? {
	return findLocalisation(link.drop(1), context.paradoxLocale, context.project, hasDefault = true)
}

//Build String Extensions

fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
	if(condition) append(text)
	return this
}

fun StringBuilder.appendPsiLink(prefix: String, target: String): StringBuilder {
	if(target.isEmpty()) return append(unresolvedEscapedString) //如果target为空，需要特殊处理
	return append("<a href=\"psi_element://").append(prefix).append(target).append("\">").append(target).append("</a>")
}

fun StringBuilder.appendIconTag(url: String, size: Int = iconSize, local: Boolean = true): StringBuilder {
	return append("<img src=\"").appendIf(local, "file:/").append(url)
		.append("\" width=\"").append(size).append("\" height=\"").append(size).append("\" hspace=\"1\"/>")
}

fun StringBuilder.appendFileInfo(fileInfo: ParadoxFileInfo): StringBuilder {
	return append("[").append(fileInfo.path).append("]")
}

fun StringBuilder.appendType(type: ParadoxType, subtypes: List<ParadoxType>): StringBuilder {
	append(type.name)
	if(subtypes.isNotEmpty()) {
		subtypes.joinTo(this, ", ", ", ") { subtype -> subtype.name }
	}
	return this
}

fun StringBuilder.appendBr(): StringBuilder {
	return append("<br>")
}

//Inline Extensions

inline fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String {
	return PlsBundle.getMessage(key, *params)
}

inline fun String.resolveIconUrl(project: Project, defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveByName(this, project, defaultToUnknown)
}

inline fun ParadoxScriptProperty.resolveIconUrl(defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveBySprite(this, defaultToUnknown)
}

inline fun VirtualFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveByFile(this, defaultToUnknown)
}

inline fun PsiFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveByFile(this, defaultToUnknown)
}

inline fun ParadoxLocalisationProperty.renderText(): String {
	return ParadoxLocalisationTextRenderer.render(this)
}

inline fun ParadoxLocalisationProperty.renderTextTo(buffer: StringBuilder) {
	ParadoxLocalisationTextRenderer.renderTo(this, buffer)
}

inline fun ParadoxLocalisationProperty.extractText(): String {
	return ParadoxLocalisationTextExtractor.extract(this)
}

inline fun ParadoxLocalisationProperty.extractTextTo(buffer: StringBuilder) {
	ParadoxLocalisationTextExtractor.extractTo(this, buffer)
}

inline fun CwtFile.resolveConfig(): CwtConfig {
	return CwtConfigResolver.resolve(this)
}

inline fun ParadoxScriptFile.resolveData(): List<Any> {
	return ParadoxScriptDataResolver.resolve(this)
}

inline fun ParadoxLocalisationFile.resolveData(): Map<String, String> {
	return ParadoxLocalisationDataResolver.resolve(this)
}
