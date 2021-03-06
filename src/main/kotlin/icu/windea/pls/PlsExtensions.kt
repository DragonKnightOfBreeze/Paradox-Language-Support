package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*
import org.jetbrains.annotations.*
import java.util.*
import kotlin.Pair

//region Constants
val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
val cachedParadoxFileInfoKey = Key<CachedValue<ParadoxFileInfo>>("cachedParadoxFileInfo")
val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
val cachedParadoxDefinitionPropertyInfoKey = Key<CachedValue<ParadoxDefinitionPropertyInfo>>("cachedParadoxDefinitionPropertyInfo")
val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
//endregion

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getSettings() = ParadoxSettingsState.getInstance()

fun getConfig(): CwtConfigGroups {
	return ServiceManager.getService(getDefaultProject(), CwtConfigProvider::class.java).configGroups
}

fun getConfig(project: Project) = ServiceManager.getService(project, CwtConfigProvider::class.java).configGroups

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
	val lines = LinkedList<String>()
	var prevElement = element.prevSibling ?: element.parent?.prevSibling
	while(prevElement != null) {
		val text = prevElement.text
		if(prevElement !is PsiWhiteSpace) {
			if(!isPreviousComment(prevElement)) break
			val documentText = text.trimStart('#').trim().escapeXml()
			lines.addFirst(documentText)
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
	return elementType == ParadoxLocalisationTypes.COMMENT || elementType == ParadoxScriptTypes.COMMENT
}

fun matchesSubtypeExpression(subtypeExpression: String, subtypes: List<String>): Boolean {
	return when {
		subtypeExpression.startsWith('!') -> subtypeExpression.drop(1) !in subtypes
		else -> subtypeExpression in subtypes
	}
}

fun resolveTypeExpression(typeExpression: String): Pair<String, String?> {
	val dotIndex = typeExpression.indexOf('.')
	val type = if(dotIndex == -1) typeExpression else typeExpression.substring(0, dotIndex)
	val subtype = if(dotIndex == -1) null else typeExpression.substring(dotIndex + 1)
	return type to subtype
}
//endregion

//region PsiElement Extensions
fun PsiElement.isQuoted(): Boolean {
	return firstLeafOrSelf.text.startsWith('"') //判断第一个叶子节点或本身的文本是否以引号开头
}

val CwtProperty.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtProperty): CwtConfigType? {
	val name = element.name
	return when {
		name.surroundsWith("type[", "]") -> CwtConfigType.Type
		name.surroundsWith("subtype[", "]") -> CwtConfigType.Subtype
		name.surroundsWith("enum[", "]") -> CwtConfigType.Enum
		name.surroundsWith("complex_enum[", "]") -> CwtConfigType.ComplexEnum
		name.surroundsWith("value[", "]") -> CwtConfigType.Value
		name.surroundsWith("single_alias[", "]") -> CwtConfigType.SingleAlias
		name.surroundsWith("alias[", "]") -> CwtConfigType.Alias
		else -> {
			val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
			val parentName = parentProperty.name
			when {
				parentName == "links" -> CwtConfigType.Link
				parentName == "localisation_links" -> CwtConfigType.LocalisationLink
				parentName == "localisation_commands" -> CwtConfigType.LocalisationCommand
				parentName == "modifier_categories" -> CwtConfigType.ModifierCategory
				parentName == "modifiers" -> CwtConfigType.Modifier
				parentName == "scopes" -> CwtConfigType.Scope
				parentName == "scope_groups" -> CwtConfigType.ScopeGroup
				else -> null
			}
		}
	}
}

val CwtValue.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtValue): CwtConfigType? {
	val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
	val parentName = parentProperty.name
	return when {
		parentName.surroundsWith("enum[", "]") -> CwtConfigType.EnumValue
		parentName.surroundsWith("value[", "]") -> CwtConfigType.ValueValue
		else -> null
	}
}

val PsiElement.gameType: ParadoxGameType? get() = doGetGameType(this)

private fun doGetGameType(element: PsiElement): ParadoxGameType? {
	return element.containingFile.fileInfo?.gameType
}

val PsiElement.localeInfo: ParadoxLocaleInfo? get() = doGetLocale(this)

private fun doGetLocale(element: PsiElement): ParadoxLocaleInfo? {
	return when(val file = element.containingFile) {
		is ParadoxScriptFile -> inferParadoxLocale()
		is ParadoxLocalisationFile -> file.locale?.localeInfo
		else -> null
	}
}

val VirtualFile.fileInfo: ParadoxFileInfo? get() = this.getUserData(paradoxFileInfoKey)

val PsiFile.fileInfo: ParadoxFileInfo? get() = doGetFileInfo(this.originalFile) //使用原始文件

val PsiElement.fileInfo: ParadoxFileInfo? get() = doGetFileInfo(this.containingFile.originalFile) //使用原始文件

internal fun canGetFileInfo(file: PsiFile): Boolean {
	//paradoxScriptFile, paradoxLocalisationFile, ddsFile
	if(file is ParadoxScriptFile || file is ParadoxLocalisationFile) return true
	val extension = file.name.substringAfterLast('.').lowercase()
	if(extension == "dds") return true
	return false
}

private fun doGetFileInfo(file: PsiFile): ParadoxFileInfo? {
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
	val subPaths = LinkedList<String>()
	subPaths.addFirst(fileName)
	var currentFile = file.parent
	while(currentFile != null) {
		val rootType = getRootType(currentFile)
		val rootPath = currentFile.virtualFile.toNioPath()
		if(rootType != null) {
			val path = getPath(subPaths)
			val gameType = getGameType(currentFile) ?: ParadoxGameType.defaultValue()
			return ParadoxFileInfo(fileName, path, rootPath, fileType, rootType, gameType)
		}
		subPaths.addFirst(currentFile.name)
		currentFile = currentFile.parent
	}
	return null
}

private fun getPath(subPaths: List<String>): ParadoxPath {
	return ParadoxPath(subPaths)
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

val ParadoxDefinitionProperty.definitionInfo: ParadoxDefinitionInfo? get() = doGetDefinitionInfo(this)

private fun doGetDefinitionInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionInfoKey) {
		val value = resolveDefinitionInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveDefinitionInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
	//NOTE cwt文件中定义的definition的propertyPath的minDepth是4（跳过3个rootKey）
	//NOTE 这个方法有可能导致ProcessCanceledException，因为调用element.name导致！
	val propertyPath = element.resolvePropertyPath(4) ?: return null
	val fileInfo = element.fileInfo ?: return null
	val path = fileInfo.path
	val gameType = fileInfo.gameType
	val elementName = element.name ?: return null
	val project = element.project
	val configGroup = getConfig(project).getValue(gameType) //这里需要指定project
	return configGroup.resolveDefinitionInfo(element, elementName, path, propertyPath)
}

val ParadoxDefinitionProperty.definitionPropertyInfo: ParadoxDefinitionPropertyInfo? get() = doGetDefinitionPropertyInfo(this)

private fun doGetDefinitionPropertyInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionPropertyInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionPropertyInfoKey) {
		val value = resolveDefinitionPropertyInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveDefinitionPropertyInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionPropertyInfo? {
	//NOTE 注意这里获得的definitionInfo可能会过时！因为对应的type和subtypes可能基于其他的definitionProperty
	//NOTE 这里的definitionProperty可能是scriptFile也可能是scriptProperty
	val (_, definitionInfo, path) = element.findParentDefinitionAndExtraInfo() ?: return null
	//推断scope：匹配的subtypeConfig上的第一个存在的名为push_scope的option的值，可能为null
	val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
	val pointer = element.createPointer()
	val gameType = definitionInfo.gameType
	val configGroup = getConfig(element.project).getValue(gameType)
	val propertyConfigs = definitionInfo.resolvePropertyConfigs(path, configGroup)
	val childPropertyConfigs = definitionInfo.resolveChildPropertyConfigs(path, configGroup)
	val childValueConfigs = definitionInfo.resolveChildValuesConfigs(path, configGroup)
	val childPropertyOccurrence = definitionInfo.resolveChildPropertyOccurrence(childPropertyConfigs, element, configGroup)
	val childValueOccurrence = definitionInfo.resolveChildValueOccurrence(childValueConfigs, element, configGroup)
	return ParadoxDefinitionPropertyInfo(
		path, scope, propertyConfigs, childPropertyConfigs, childValueConfigs,
		childPropertyOccurrence, childValueOccurrence, gameType, pointer
	)
}

val ParadoxScriptProperty.propertyConfig:CwtPropertyConfig? get() = doGetPropertyConfig(this)

private fun doGetPropertyConfig(element: ParadoxScriptProperty): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val definitionPropertyInfo = element.definitionPropertyInfo ?: return null
	return definitionPropertyInfo.propertyConfig
}

val ParadoxScriptPropertyKey.propertyConfig: CwtPropertyConfig? get() = doGetPropertyConfig(this)

private fun doGetPropertyConfig(element: ParadoxScriptPropertyKey): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val property = element.parent.castOrNull<ParadoxScriptProperty>() ?: return null
	val definitionPropertyInfo = property.definitionPropertyInfo ?: return null
	return definitionPropertyInfo.propertyConfig
}

val ParadoxScriptValue.valueConfig: CwtValueConfig? get() = doGetValueConfig(this)

private fun doGetValueConfig(element: ParadoxScriptValue): CwtValueConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val parent = element.parent
	when(parent) {
		//如果value是property的value
		is ParadoxScriptPropertyValue -> {
			val property = parent.parent as? ParadoxScriptProperty ?: return null
			val definitionPropertyInfo = property.definitionPropertyInfo ?: return null
			return definitionPropertyInfo.matchedPropertyConfig?.valueConfig
		}
		//如果value是block中的value
		is ParadoxScriptBlock -> {
			val property = parent.parent?.parent as? ParadoxScriptProperty ?: return null
			val definitionPropertyInfo = property.definitionPropertyInfo ?: return null
			val childValueConfigs = definitionPropertyInfo.childValueConfigs
			if(childValueConfigs.isEmpty()) return null
			val gameType = definitionPropertyInfo.gameType
			val configGroup = getConfig(element.project).getValue(gameType)
			return childValueConfigs.find {
				matchesValue(it.valueExpression, element, configGroup)
			}
		}
		else -> return null
	}
}

fun ParadoxScriptValue.getType(): String? {
	return when(this) {
		is ParadoxScriptBlock -> "block"
		is ParadoxScriptString -> "string"
		is ParadoxScriptBoolean -> "boolean"
		is ParadoxScriptInt -> "int"
		is ParadoxScriptFloat -> "float"
		is ParadoxScriptColor -> "color"
		is ParadoxScriptCode -> "code"
		else -> null
	}
}

fun ParadoxScriptValue.checkType(type: String): Boolean {
	return when(type) {
		"block" -> this is ParadoxScriptBlock
		"string" -> this is ParadoxScriptString
		"boolean" -> this is ParadoxScriptBoolean
		"int" -> this is ParadoxScriptInt
		"float" -> this is ParadoxScriptFloat
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
	val subPaths = LinkedList<String>()
	var current = this
	while(current !is PsiFile && current !is ParadoxScriptRootBlock) {
		when {
			current is ParadoxScriptProperty -> {
				subPaths.addFirst(current.name)
			}
			current is ParadoxScriptValue -> {
				val parent = current.parent ?: break
				if(parent is ParadoxScriptBlock) {
					subPaths.addFirst(parent.indexOfChild(current).toString())
				}
				current = parent
			}
		}
		current = current.parent ?: break
	}
	return if(subPaths.isEmpty()) null else ParadoxPath(subPaths)
}

fun PsiElement.resolvePropertyPath(maxDepth: Int = -1): ParadoxPropertyPath? {
	val subPaths = LinkedList<String>()
	val subPathInfos = emptyList<ParadoxPropertyPathInfo>() //TODO 目前不需要获取
	var current = this
	var depth = 0
	while(current !is PsiFile && current !is ParadoxScriptRootBlock) {
		when {
			current is ParadoxScriptProperty -> {
				subPaths.addFirst(current.name)
				depth++
			}
			//忽略scriptValue
		}
		//如果发现深度超出指定的最大深度，则直接返回null
		if(maxDepth != -1 && maxDepth < depth) return null
		current = current.parent ?: break
	}
	return ParadoxPropertyPath(subPaths, subPathInfos)
}

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo? get() = doGetLocalisationInfo(this)

private fun doGetLocalisationInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxLocalisationInfoKey) {
		val value = resolveLocalisationInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveLocalisationInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
	val name = element.name
	val type = ParadoxLocalisationCategory.resolve(element) ?: return null
	return ParadoxLocalisationInfo(name, type)
}

val ParadoxLocalisationLocale.localeInfo: ParadoxLocaleInfo?
	get() {
		return getConfig().localeMap[name]
	}

val ParadoxLocalisationPropertyReference.colorInfo: ParadoxColorInfo?
	get() {
		val colorId = this.propertyReferenceParameter?.text?.firstOrNull()
		if(colorId != null && colorId.isUpperCase()) {
			return getConfig().colorMap[colorId.toString()]
		}
		return null
	}

val ParadoxLocalisationSequentialNumber.sequentialNumberInfo: ParadoxSequentialNumberInfo?
	get() = getConfig().sequentialNumberMap[name]

val ParadoxLocalisationColorfulText.colorInfo: ParadoxColorInfo?
	get() = getConfig().colorMap[name]

fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = false): ParadoxScriptProperty? {
	return properties.find { it.name.equals(propertyName, ignoreCase) }
}

//fun ParadoxDefinitionProperty.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
//	return properties.filter { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxDefinitionProperty.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
//	return values.find { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxDefinitionProperty.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
//	return values.filter { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findProperty(propertyName: String, ignoreCase: Boolean = false): ParadoxScriptProperty? {
//	return propertyList.find { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
//	return propertyList.filter { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
//	return valueList.find { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
//	return valueList.filter { it.value.equals(value, ignoreCase) }
//}

/**
 * 得到上一级definitionProperty，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionProperty(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxDefinitionProperty) {
			return current
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}

/**
 * 得到上一级definitionProperty，跳过正在填写的，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionPropertySkipThis(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxScriptRootBlock) {
			return (current.parent ?: break) as ParadoxDefinitionProperty
		} else if(current is ParadoxScriptBlock) {
			return (current.parent.parent ?: break) as ParadoxDefinitionProperty
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}

/**
 * 得到上一级definition，可能为自身，可能为null。
 */
fun PsiElement.findParentDefinitionAndExtraInfo(): Tuple3<ParadoxDefinitionProperty, ParadoxDefinitionInfo, ParadoxPropertyPath>? {
	var current: PsiElement = this
	val subPaths = LinkedList<String>()
	val subPathInfos = LinkedList<ParadoxPropertyPathInfo>()
	do {
		if(current is ParadoxDefinitionProperty) {
			val name = current.name ?: return null
			val definitionInfo = current.definitionInfo
			if(definitionInfo != null) {
				val path = ParadoxPropertyPath(subPaths, subPathInfos)
				return tupleOf(current, definitionInfo, path)
			}
			subPaths.addFirst(name)
			subPathInfos.addFirst(ParadoxPropertyPathInfo(name, current.isQuoted()))
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}
//endregion

//region Find Extensions
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

///**
// * 基于脚本变量名字索引，根据名字查判断是否存在脚本变量（scriptedVariable）。
// */
//fun existsScriptVariable(
//	name: String,
//	project: Project,
//	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
//): Boolean {
//	return ParadoxScriptVariableNameIndex.exists(name, project, scope)
//}

/**
 * 基于脚本变量名字索引，根据名字查找脚本变量（scriptedVariable）。
 */
fun findScriptVariable(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptVariable? {
	return ParadoxScriptVariableNameIndex.findOne(name, project, scope, !preferOverridden)
}

/**
 * 基于脚本变量名字索引，根据名字查找所有的脚本变量（scriptedVariable）。
 */
fun findScriptVariables(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.findAll(name, project, scope)
}

/**
 * 基于脚本变量名字索引，查找所有的脚本变量（scriptedVariable）。
 */
fun findScriptVariables(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.findAll(project, scope)
}

/**
 * 基于定义名字索引，根据名字、类型表达式判断是否存在脚本文件的定义（definition）。
 */
fun hasDefinition(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): Boolean {
	return ParadoxDefinitionNameIndex.exists(name, typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找脚本文件的定义（definition）。
 */
fun findDefinition(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptProperty? {
	return ParadoxDefinitionNameIndex.findOne(name, typeExpression, project, scope, !preferOverridden)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitions(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.findAll(name, typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitions(
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.findAll(typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据名字、类型表达式判断是否存在脚本文件的定义（definition）。
 */
fun hasDefinitionByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): Boolean {
	return ParadoxDefinitionTypeIndex.exists(name, typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptProperty? {
	return ParadoxDefinitionTypeIndex.findOne(name, typeExpression, project, scope, !preferOverridden)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionsByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.findAll(name, typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionsByType(
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.findAll(typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据关键字和类型表达式查找所有的脚本文件的定义（definition）。
 */
fun findDefinitionsByKeywordByType(
	keyword: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptProperty> {
	return ParadoxDefinitionTypeIndex.findAllByKeyword(keyword, typeExpression, project, scope, maxCompleteSize)
}

/**
 * 基于本地化名字索引，根据名字、语言区域判断是否存在本地化（localisation）。
 */
fun hasLocalisation(
	name: String,
	localeInfo: ParadoxLocaleInfo?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
): Boolean {
	return ParadoxLocalisationNameIndex.exists(name, localeInfo, project, scope)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找本地化（localisation）。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findLocalisation(
	name: String,
	localeInfo: ParadoxLocaleInfo?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.findOne(name, localeInfo, project, scope, hasDefault, !preferOverridden)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找所有的本地化（localisation）。
 * * 如果[localeInfo]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findLocalisations(
	name: String,
	localeInfo: ParadoxLocaleInfo? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = true
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAll(name, localeInfo, project, scope, hasDefault)
}

///**
// * 基于本地化名字索引，根据语言区域查找所有的本地化（localisation）。
// * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
// * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
// */
//fun findLocalisations(
//	locale: ParadoxLocaleInfo? = null,
//	project: Project,
//	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
//	hasDefault: Boolean = false
//): List<ParadoxLocalisationProperty> {
//	return ParadoxLocalisationNameIndex.findAll(locale, project, scope, hasDefault)
//}

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
	return ParadoxLocalisationNameIndex.findAllByKeyword(keyword, project, scope, maxCompleteSize)
}

/**
 * 基于本地化名字索引，根据一组名字、语言区域查找所有的本地化（localisation）。
 * * 如果[localeInfo]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 * * 如果[keepOrder]为`true`，则根据这组名字排序查询结果。
 */
fun findLocalisationsByNames(
	names: Iterable<String>,
	localeInfo: ParadoxLocaleInfo? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	keepOrder: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAllByNames(names, localeInfo, project, scope, hasDefault, keepOrder)
}

/**
 * 基于本地化名字索引，根据名字、语言区域判断是否存在同步本地化（localisation_synced）。
 */
fun hasSyncedLocalisation(
	name: String,
	localeInfo: ParadoxLocaleInfo?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
): Boolean {
	return ParadoxSyncedLocalisationNameIndex.exists(name, localeInfo, project, scope)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找同步本地化（localisation_synced）。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findSyncedLocalisation(
	name: String,
	localeInfo: ParadoxLocaleInfo?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxSyncedLocalisationNameIndex.getOne(name, localeInfo, project, scope, hasDefault, !preferOverridden)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找所有的同步本地化（localisation_synced）。
 * * 如果[localeInfo]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
 * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
 */
fun findSyncedLocalisations(
	name: String,
	localeInfo: ParadoxLocaleInfo? = null,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = true
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findAll(name, localeInfo, project, scope, hasDefault)
}

///**
// * 基于同步本地化名字索引，根据语言区域查找所有的同步本地化（localisation_synced）。
// * * 如果[locale]为`null`，则将用户的语言区域对应的本地化放到该组的最前面。
// * * 如果[hasDefault]为`true`，且没有查找到对应语言区域的本地化，则忽略语言区域。
// */
//fun findSyncedLocalisations(
//	locale: ParadoxLocaleInfo? = null,
//	project: Project,
//	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
//	hasDefault: Boolean = false
//): List<ParadoxLocalisationProperty> {
//	return ParadoxSyncedLocalisationNameIndex.findAll(locale, project, scope, hasDefault)
//}

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
	return ParadoxSyncedLocalisationNameIndex.findAllByKeyword(keyword, project, scope, maxCompleteSize)
}
//endregion

//region Link Extensions
fun resolveLink(link: String, context: PsiElement): PsiElement? {
	return when {
		link.startsWith('@') -> resolveCwtLink(link, context)
		link.startsWith('$') -> resolveScriptLink(link, context)
		link.startsWith('#') -> resolveLocalisationLink(link, context)
		else -> null
	}
}

//@stellaris.types.building, @stellaris.types.civic_or_origin.civic
private fun resolveCwtLink(link: String, context: PsiElement): CwtProperty? {
	return runCatching {
		val project = context.project
		val tokens = link.drop(1).split('.')
		val gameType = tokens[0]
		val configType = tokens[1]
		when(configType) {
			"types" -> {
				val name = tokens.getOrNull(2)
				val subtypeName = tokens.getOrNull(3)
				return when {
					name == null -> null
					subtypeName == null -> getConfig(project).getValue(gameType).types.getValue(name)
						.pointer.element
					else -> getConfig(project).getValue(gameType).types.getValue(name)
						.subtypes.getValue(subtypeName).pointer.element
				}
			}
			"scopes" -> {
				val name = tokens.getOrNull(2)?:return null
				return getConfig(project).getValue(gameType).scopeAliasMap.getValue(name).pointer.element
			}
			else -> null
		}
	}.getOrNull()
}

//$ethos.ethic_authoritarian, $job.head_researcher
private fun resolveScriptLink(link: String, context: PsiElement): ParadoxScriptProperty? {
	return runCatching {
		val project = context.project
		val tokens = link.drop(1).split('.')
		val type = tokens[0]
		val name = tokens[1]
		findDefinitionByType(name, type, project)
	}.getOrNull()
}

//#NAME, #KEY
private fun resolveLocalisationLink(link: String, context: PsiElement): ParadoxLocalisationProperty? {
	return runCatching {
		val token = link.drop(1)
		return findLocalisation(token, context.localeInfo, context.project, hasDefault = true)
	}.getOrNull()
}
//endregion

//region Documentation Extensions
fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
	if(condition) append(text)
	return this
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = false): StringBuilder {
	DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
	return this
}

	

fun StringBuilder.appendScriptLink(name: String, type: String): StringBuilder {
	if(name.isEmpty()) return append(unresolvedEscapedString) //如果target为空，需要特殊处理
	return appendPsiLink("$$type.$name", name)
}

fun StringBuilder.appendLocalisationLink(name: String): StringBuilder {
	if(name.isEmpty()) return append(unresolvedEscapedString) //如果target为空，需要特殊处理
	return appendPsiLink("#$name", name)
}

fun StringBuilder.appendIconTag(url: String, local: Boolean = true): StringBuilder {
	return append("<img src=\"").appendIf(local, "file:/").append(url).append("\" hspace=\"1\"/>")
}

fun StringBuilder.appendIconTag(url: String, size: Int, local: Boolean = true): StringBuilder {
	return append("<img src=\"").appendIf(local, "file:/").append(url)
		.append("\" width=\"").append(size).append("\" height=\"").append(size).append("\" hspace=\"1\"/>")
}

fun StringBuilder.appendFileInfo(fileInfo: ParadoxFileInfo): StringBuilder {
	return append("[").append(fileInfo.path).append("]")
}

fun StringBuilder.appendBr(): StringBuilder {
	return append("<br>")
}
//endregion

//region Inline Extensions
@Suppress("NOTHING_TO_INLINE")
inline fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String {
	return PlsBundle.getMessage(key, *params)
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.resolveIconUrl(project: Project, defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveByName(this, project, defaultToUnknown)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxScriptProperty.resolveIconUrl(defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolveBySprite(this, defaultToUnknown)
}

//@Suppress("NOTHING_TO_INLINE")
//inline fun VirtualFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
//	return ParadoxIconUrlResolver.resolveByFile(this, defaultToUnknown)
//}

//@Suppress("NOTHING_TO_INLINE")
//inline fun PsiFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
//	return ParadoxIconUrlResolver.resolveByFile(this, defaultToUnknown)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.renderText(): String {
	return ParadoxLocalisationTextRenderer.render(this)
}

//@Suppress("NOTHING_TO_INLINE")
//inline fun ParadoxLocalisationProperty.renderTextTo(buffer: StringBuilder) {
//	ParadoxLocalisationTextRenderer.renderTo(this, buffer)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.extractText(): String {
	return ParadoxLocalisationTextExtractor.extract(this)
}

//@Suppress("NOTHING_TO_INLINE")
//inline fun ParadoxLocalisationProperty.extractTextTo(buffer: StringBuilder) {
//	ParadoxLocalisationTextExtractor.extractTo(this, buffer)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun CwtFile.resolveConfig(): CwtFileConfig {
	return CwtConfigResolver.resolve(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxScriptFile.resolveData(): List<Any> {
	return ParadoxScriptDataResolver.resolve(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationFile.resolveData(): Map<String, String> {
	return ParadoxLocalisationDataResolver.resolve(this)
}
//endregion