package com.windea.plugin.idea.paradox

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.core.settings.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.model.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*
import com.windea.plugin.idea.paradox.util.*
import org.jetbrains.annotations.*
import kotlin.Pair

//Misc Extensions

/**得到指定元素之前的所有直接的注释的文本，作为文档注释，跳过空白。*/
fun getDocTextFromPreviousComment(element: PsiElement): String {
	//我们认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释，但这并非文档注释的全部
	val lines = mutableListOf<String>()
	var prevElement = element.prevSibling ?: element.parent?.prevSibling
	while(prevElement != null) {
		val text = prevElement.text
		if(prevElement !is PsiWhiteSpace) {
			if(!isPreviousComment(prevElement)) break
			lines.add(0, text.trimStart('#').trim().escapeXml())
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

val settings get() = ParadoxSettingsState.getInstance()

//Keys

val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
val paradoxDefinitionInfoKey = Key<ParadoxDefinitionInfo>("paradoxDefinitionInfo")
val cachedParadoxFileInfoKey = Key<CachedValue<ParadoxFileInfo>>("cachedParadoxFileInfo")
val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")

//ParadoxPsiElement Extensions

val ParadoxLocalisationLocale.paradoxLocale: ParadoxLocale?
	get() {
		val name = this.name
		return paradoxLocaleMap[name]
	}

val ParadoxLocalisationPropertyReference.paradoxColor: ParadoxColor?
	get() {
		val colorId = this.propertyReferenceParameter?.text?.firstOrNull()
		if(colorId != null && colorId.isUpperCase()) {
			return paradoxColorMap[colorId.toString()]
		}
		return null
	}

val ParadoxLocalisationSequentialNumber.paradoxSequentialNumber: ParadoxSequentialNumber?
	get() {
		val name = this.name
		return paradoxSequentialNumberMap[name]
	}

val ParadoxLocalisationCommandScope.paradoxCommandScope: ParadoxCommandScope?
	get() {
		val name = this.name.toCapitalizedWord() //忽略大小写，首字母大写
		if(name.startsWith(eventTargetPrefix)) return null
		return paradoxCommandScopeMap[name]
	}

val ParadoxLocalisationCommandField.paradoxCommandField: ParadoxCommandField?
	get() {
		val name = this.name
		return paradoxCommandFieldMap[name]
	}

val ParadoxLocalisationColorfulText.paradoxColor: ParadoxColor?
	get() {
		val name = this.name
		return paradoxColorMap[name]
	}


val PsiElement.paradoxLocale: ParadoxLocale? get() = getLocale(this)

private fun getLocale(element: PsiElement): ParadoxLocale? {
	return when(val file = element.containingFile) {
		is ParadoxScriptFile -> inferredParadoxLocale
		is ParadoxLocalisationFile -> file.locale?.paradoxLocale
		else -> null
	}
}


val VirtualFile.paradoxFileInfo: ParadoxFileInfo? get() = this.getUserData(paradoxFileInfoKey)

val PsiFile.paradoxFileInfo: ParadoxFileInfo? get() = getFileInfo(this.originalFile) //使用原始文件

val PsiElement.paradoxFileInfo: ParadoxFileInfo? get() = getFileInfo(this.containingFile)

internal fun canGetFileInfo(file: PsiFile): Boolean {
	return file is ParadoxScriptFile || file is ParadoxLocalisationFile
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
		if(rootType != null) {
			val path = getPath(subpaths)
			val gameType = getGameType()
			return ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
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
	val fileName = file.name.toLowerCase()
	val fileExtension = fileName.substringAfterLast('.')
	return when {
		fileExtension in scriptFileExtensions -> ParadoxFileType.Script
		fileExtension in localisationFileExtensions -> ParadoxFileType.Localisation
		else -> null
	}
}

private fun getRootType(file: PsiDirectory): ParadoxRootType? {
	if(!file.isDirectory) return null
	val fileName = file.name
	for(child in file.files) {
		val childName = child.name
		when {
			exeFileNames.any { exeFileName -> childName.equals(exeFileName, true) } -> return ParadoxRootType.Stdlib
			childName.equals(descriptorFileName, true) -> return ParadoxRootType.Mod
			fileName == ParadoxRootType.PdxLauncher.key -> return ParadoxRootType.PdxLauncher
			fileName == ParadoxRootType.PdxOnlineAssets.key -> return ParadoxRootType.PdxOnlineAssets
			fileName == ParadoxRootType.TweakerGuiAssets.key -> return ParadoxRootType.TweakerGuiAssets
		}
	}
	return null
}

private fun getGameType(): ParadoxGameType {
	return ParadoxGameType.Stellaris //TODO
}

val ParadoxScriptProperty.paradoxDefinitionInfo: ParadoxDefinitionInfo? get() = getDefinitionInfo(this)

val ParadoxScriptProperty.paradoxDefinitionInfoNoCheck: ParadoxDefinitionInfo? get() = getDefinitionInfo(this, false)

internal fun canGetDefinitionInfo(element: ParadoxScriptProperty): Boolean {
	//最低到2级scriptProperty
	val parent = element.parent
	return parent is ParadoxScriptRootBlock || parent?.parent?.parent?.parent is ParadoxScriptRootBlock
}

private fun getDefinitionInfo(element: ParadoxScriptProperty, check: Boolean = true): ParadoxDefinitionInfo? {
	if(check && !canGetDefinitionInfo(element)) return null
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionInfoKey) {
		CachedValueProvider.Result.create(resolveDefinitionInfo(element), element)
	}
}

private fun resolveDefinitionInfo(element: ParadoxScriptProperty): ParadoxDefinitionInfo? {
	val (_, path, _, _, gameType) = element.paradoxFileInfo ?: return null
	val ruleGroup = paradoxRuleGroups[gameType.key] ?: return null
	val elementName = element.name
	val propertyPath = element.resolvePropertyPath() ?: return null
	val definition = ruleGroup.definitions.values.find { it.matches(element, elementName, path, propertyPath) } ?: return null
	return definition.toDefinitionInfo(element, elementName)
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
		"block","array | object" -> this is ParadoxScriptBlock
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

fun PsiElement.resolvePropertyPath(): ParadoxPath? {
	val subpaths = mutableListOf<String>()
	var current = this
	while(current !is PsiFile) {
		when {
			current is ParadoxScriptProperty -> {
				subpaths.add(0, current.name)
			}
			//忽略scriptValue
		}
		current = current.parent ?: break
	}
	return if(subpaths.isEmpty()) null else ParadoxPath(subpaths)
}

fun PsiElement.resolveDefinitionInfoAndDefinitionPropertyPath(): Pair<ParadoxDefinitionInfo,ParadoxPath>? {
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


fun ParadoxScriptBlock.isAlwaysYes(): Boolean {
	return this.isObject && this.propertyList.singleOrNull()?.let { it.name == "always" && it.value == "yes" } ?: false
}

//Find Extensions

fun findScriptVariableInFile(name: String, file: PsiFile): ParadoxScriptVariable? {
	//在所在文件中递归查找（不一定定义在顶层）
	if(file !is ParadoxScriptFile) return null
	return file.descendantsOfType<ParadoxScriptVariable>().find{ it.name == name }
}

fun findScriptVariablesInFile(name: String, file: PsiFile): List<ParadoxScriptVariable> {
	//在所在文件中递归查找（不一定定义在顶层），仅查找第一个
	if(file !is ParadoxScriptFile) return emptyList()
	return file.descendantsOfType<ParadoxScriptVariable>().filter{ it.name == name }.toList()
}

fun findScriptVariablesInFile(file: PsiFile): List<ParadoxScriptVariable> {
	//在所在文件中递归查找（不一定定义在顶层）
	if(file !is ParadoxScriptFile) return emptyList()
	return file.descendantsOfType<ParadoxScriptVariable>().toList()
}

fun findScriptVariable(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxScriptVariable? {
	return ParadoxScriptVariableNameIndex.getOne(name, project, scope, !settings.preferOverridden)
}

fun findScriptVariables(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.getAll(name, project, scope)
}

fun findScriptVariables(project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.getAll(project, scope)
}


fun findDefinition(name: String, type: String? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxScriptProperty? {
	return ParadoxDefinitionNameIndex.getOne(name, type, project, scope, !settings.preferOverridden)
}

fun findDefinitions(name: String, type: String? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.getAll(name, type, project, scope)
}

fun findDefinitions(type: String? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxDefinitionNameIndex.getAll(type, project, scope)
}

fun findDefinitionsByType(type:String,project:Project,scope: GlobalSearchScope = GlobalSearchScope.allScope(project)):List<ParadoxScriptProperty>{
	return ParadoxDefinitionTypeIndex.getAll(type,project,scope)
}


fun findScriptLocalisation(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxScriptProperty? {
	return ParadoxScriptLocalisationNameIndex.getOne(name, project, scope, !settings.preferOverridden)
}

fun findScriptLocalisations(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxScriptLocalisationNameIndex.getAll(name, project, scope)
}

fun findScriptLocalisations(project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxScriptLocalisationNameIndex.getAll(project, scope)
}


fun findLocalisation(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project), hasDefault: Boolean = false): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.getOne(name, locale, project, scope, hasDefault, !settings.preferOverridden)
}

fun findLocalisations(name: String, locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project), hasDefault: Boolean = true): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.getAll(name, locale, project, scope, hasDefault)
}

fun findLocalisations(locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project), hasDefault: Boolean = false): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.getAll(locale, project, scope, hasDefault)
}

fun findLocalisations(names: Iterable<String>, locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project), hasDefault: Boolean = false, keepOrder: Boolean = false): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.getAll(names, locale, project, scope, hasDefault, keepOrder)
}

//Link Extensions

fun resolveLink(link:String,context:PsiElement):PsiElement?{
	return when {
		link.startsWith("#") -> resolveLocalisationLink(link, context)
		link.startsWith("$") -> resolveScriptLink(link, context)
		else -> null
	}
}

private fun resolveLocalisationLink(link: String, context: PsiElement): ParadoxLocalisationProperty? {
	return findLocalisation(link.drop(1), context.paradoxLocale, context.project, hasDefault = true)
}

private fun resolveScriptLink(link: String, context: PsiElement): ParadoxScriptProperty? {
	return findDefinition(link.drop(1), null, context.project)
}

//Build String Extensions

fun StringBuilder.appendPsiLink(prefix: String, target: String): StringBuilder {
	return append("<a href=\"psi_element://").append(prefix).append(target).append("\">").append(target).append("</a>")
}

fun StringBuilder.appendIconTag(url: String, size: Int = iconSize): StringBuilder {
	return append("<img src=\"").append(url).append("\" width=\"").append(size).append("\" height=\"").append(size).append("\"/>")
}

fun StringBuilder.appendFileInfo(fileInfo:ParadoxFileInfo):StringBuilder {
	return append("[").append(fileInfo.path).append("]")
}

fun StringBuilder.appendType(type:ParadoxType,subtypes:List<ParadoxType>):StringBuilder{
	append(type.name)
	if(subtypes.isNotEmpty()) {
		subtypes.joinTo(this, ", ", ", ") { subtype -> subtype.name}
	}
	return this
}

fun StringBuilder.appendBr():StringBuilder{
	return append("<br>")
}

//Inline Extensions

@Suppress("NOTHING_TO_INLINE")
inline fun message(@PropertyKey(resourceBundle = paradoxBundleName) key: String, vararg params: Any): String {
	return ParadoxBundle.getMessage(key, *params)
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.resolveIconUrl(defaultToUnknown: Boolean = true): String {
	return ParadoxIconUrlResolver.resolve(this, defaultToUnknown)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.renderText(): String {
	return ParadoxLocalisationTextRenderer.render(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.renderTextTo(buffer: StringBuilder) {
	ParadoxLocalisationTextRenderer.renderTo(this, buffer)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.extractText(): String {
	return ParadoxLocalisationTextExtractor.extract(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.extractTextTo(buffer: StringBuilder) {
	ParadoxLocalisationTextExtractor.extractTo(this, buffer)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxScriptFile.extractData(): List<Any> {
	return ParadoxScriptDataExtractor.extract(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationFile.extractData(): Map<String, String> {
	return ParadoxLocalisationDataExtractor.extract(this)
}
