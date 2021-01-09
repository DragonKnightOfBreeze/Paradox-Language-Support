package com.windea.plugin.idea.paradox

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*
import com.windea.plugin.idea.paradox.util.*
import org.jetbrains.annotations.*

//Extensions

fun StringBuilder.appendPsiLink(prefix:String,target:String): StringBuilder {
	return append("<a href='psi_element://").append(prefix).append(target).append("'>").append(target).append("</a>")
}

fun StringBuilder.appendIconTag(url:String,size:Int=iconSize): StringBuilder {
	return append("<img src='").append(url).append("' width='").append(size).append("' height='").append(size).append("'/>")
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

//Keys

val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
val paradoxFileTypeKey = Key<ParadoxFileType>("paradoxFileType")
val paradoxRootTypeKey = Key<ParadoxRootType>("paradoxRootType")
val paradoxGameTypeKey = Key<ParadoxGameType>("paradoxGameType")
val paradoxPathKey = Key<ParadoxPath>("paradoxPath")
val paradoxDefinitionInfoKey = Key<ParadoxDefinitionInfo>("paradoxDefinitionInfo")
val cachedParadoxFileInfoKey = Key<CachedValue<ParadoxFileInfo>>("cachedParadoxFileInfo")
val cachedParadoxFileTypeKey = Key<CachedValue<ParadoxFileType>>("cachedParadoxFileType")
val cachedParadoxRootTypeKey = Key<CachedValue<ParadoxRootType>>("cachedParadoxRootType")
val cachedParadoxGameTypeKey = Key<CachedValue<ParadoxGameType>>("cachedParadoxGameTypeKey")
val cachedParadoxPathKey = Key<CachedValue<ParadoxPath>>("cachedParadoxPath")
val cachedParadoxScriptPathKey = Key<CachedValue<ParadoxPath>>("cachedParadoxScriptPath")
val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")

//Caches 

val ruleGroups = ParadoxRuleGroupProvider.getRuleGroups()

//Extension Properties

val VirtualFile.paradoxFileInfo:ParadoxFileInfo? get() = this.getUserData(paradoxFileInfoKey)

val PsiElement.paradoxFileInfo:ParadoxFileInfo? get() = getFileInfo(this)

val ASTNode.paradoxFileInfo:ParadoxFileInfo? get() = getFileInfo(psi)

//element->element.containingFile->element.containingFile.virtualFile->tryResolve
internal fun  getFileInfo(element: PsiElement): ParadoxFileInfo? {
	//检查语言，预先过滤
	//if(element.language != ParadoxScriptLanguage && element.language != ParadoxLocalisationLanguage) return null
	val fastFileInfo = element.getUserData(paradoxFileInfoKey)
	if(fastFileInfo != null) return fastFileInfo
	if(element is PsiFile) return getFileInfoFromFile(element)
	return CachedValuesManager.getCachedValue(element, cachedParadoxFileInfoKey) {
		val file = element.containingFile
		val value = getFileInfoFromFile(file) 
		CachedValueProvider.Result.create(value, element,file) 
	}
}

private fun getFileInfoFromFile(file:PsiFile):ParadoxFileInfo?{
	return CachedValuesManager.getCachedValue(file, cachedParadoxFileInfoKey) {
		val value = file.virtualFile?.getUserData(paradoxFileInfoKey) ?: resolveFileInfo(file)
		CachedValueProvider.Result.create(value, file)
	}
}

private fun resolveFileInfo(file:PsiFile):ParadoxFileInfo?{
	val fileType = getFileType(file) ?: return null
	val fileName = file.name
	val subPaths = mutableListOf(fileName)
	var currentFile = file.parent
	while(currentFile != null) {
		val rootType = getRootType(currentFile)
		if(rootType != null) {
			val path = getPath(subPaths)
			val gameType = getGameType()
			val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
			file.putUserData(paradoxFileInfoKey,fileInfo)
			return file.getUserData(paradoxFileInfoKey)
		}
		subPaths.add(0, currentFile.name)
		currentFile = currentFile.parent
	}
	return null
}

private fun getPath(subPaths: List<String>): ParadoxPath {
	return ParadoxPath(subPaths)
}

private fun getFileType(file:PsiFile):ParadoxFileType?{
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

val PsiElement.paradoxScriptPath: ParadoxPath? get() = getScriptedPath(this)

private fun getScriptedPath(element: PsiElement): ParadoxPath? {
	return getCachedScriptPath(element)
}

private fun getCachedScriptPath(element: PsiElement): ParadoxPath? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxScriptPathKey) {
		CachedValueProvider.Result.create(resolveScriptPath(element), element)
	}
}

private fun resolveScriptPath(element: PsiElement): ParadoxPath? {
	return when {
		element is ParadoxScriptProperty || element is ParadoxScriptValue -> {
			val subPaths = mutableListOf<String>()
			var current = element
			while(current !is PsiFile) {
				when {
					current is ParadoxScriptProperty -> {
						subPaths.add(0, current.name)
					}
					current is ParadoxScriptValue -> {
						val parent = current.parent ?: break
						if(parent is ParadoxScriptBlock) {
							subPaths.add(0, parent.indexOfChild(current).toString())
						}
						current = parent
					}
				}
				current = current.parent ?: break
			}
			ParadoxPath(subPaths)
		}
		else -> null
	}
}

val ParadoxScriptProperty.paradoxDefinitionInfo: ParadoxDefinitionInfo? get() = getDefinitionInfo(this)

val ParadoxScriptProperty.paradoxDefinitionInfoNoCheck: ParadoxDefinitionInfo? get() = getDefinitionInfo(this, false)

internal fun canGetDefinitionInfo(node: ASTNode): Boolean {
	//最低到2级scriptProperty
	val parent = node.treeParent
	return parent?.elementType == ROOT_BLOCK || parent?.treeParent?.treeParent?.treeParent?.elementType == ROOT_BLOCK
}

internal fun canGetDefinitionInfo(element: ParadoxScriptProperty): Boolean {
	//最低到2级scriptProperty
	val parent = element.parent
	return parent is ParadoxScriptRootBlock || parent?.parent?.parent?.parent is ParadoxScriptRootBlock
}

private fun getDefinitionInfo(element: ParadoxScriptProperty, check: Boolean = true): ParadoxDefinitionInfo? {
	if(check && !canGetDefinitionInfo(element)) return null
	return getCachedDefinitionInfo(element)
}

private fun getCachedDefinitionInfo(element: ParadoxScriptProperty): ParadoxDefinitionInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionInfoKey) {
		CachedValueProvider.Result.create(resolveDefinitionInfo(element), element)
	}
}

private fun resolveDefinitionInfo(element: ParadoxScriptProperty): ParadoxDefinitionInfo? {
	val (_, path, _, _, gameType) = element.paradoxFileInfo ?: return null
	val ruleGroup = ruleGroups[gameType.key] ?: return null
	val elementName = element.name
	val scriptPath = element.paradoxScriptPath?: return null
	val definition = ruleGroup.types.values.find { it.matches(elementName, path, scriptPath) } ?: return null
	return definition.toMetadata(element, elementName)
}

val ASTNode.paradoxDefinitionInfo: ParadoxDefinitionInfo? get() = getDefinitionInfo(this)

val ASTNode.paradoxDefinitionInfoNoCheck: ParadoxDefinitionInfo? get() = getDefinitionInfo(this, false)

private fun getDefinitionInfo(node: ASTNode, check: Boolean = true): ParadoxDefinitionInfo? {
	if(check && !canGetDefinitionInfo(node)) return null
	return getCachedDefinitionInfo(node.psi as? ParadoxScriptProperty?:return null)
}

//Find Extensions

//使用stubIndex以提高性能

fun findScriptVariableInFile(name: String, file: PsiFile): ParadoxScriptVariable? {
	//在所在文件中递归查找（不一定定义在顶层）
	if(file !is ParadoxScriptFile) return null
	return file.findDescendantOfType { it.name == name }
}

fun findScriptVariable(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxScriptVariable? {
	return ParadoxScriptVariableKeyIndex.getOne(name, project, scope)
}

fun findScriptVariables(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableKeyIndex.getAll(name, project, scope)
}

fun findScriptVariables(project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableKeyIndex.getAll(project, scope)
}

fun findScriptProperty(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxScriptProperty? {
	return ParadoxScriptPropertyKeyIndex.getOne(name, project, scope)
}

fun findScriptProperties(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxScriptPropertyKeyIndex.getAll(name, project, scope)
}

fun findScriptProperties(project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxScriptProperty> {
	return ParadoxScriptPropertyKeyIndex.getAll(project, scope)
}

fun findLocalisationProperty(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxLocalisationProperty? {
	return ParadoxLocalisationPropertyKeyIndex.getOne(name, locale, false,project, scope)
}

fun findLocalisationPropertyOrFirst(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): ParadoxLocalisationProperty? {
	return ParadoxLocalisationPropertyKeyIndex.getOne(name, locale, true,project, scope)
}

fun findLocalisationProperties(name: String, locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationPropertyKeyIndex.getAll(name, locale, false,project, scope)
}

fun findLocalisationPropertiesOrAll(name: String, locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationPropertyKeyIndex.getAll(name, locale, true,project, scope)
}

fun findLocalisationProperties(locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationPropertyKeyIndex.getAll(locale, project, scope)
}

fun findLocalisationProperties(names: Iterable<String>, locale: ParadoxLocale? = null, project: Project, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationPropertyKeyIndex.getAll(names, locale, project, scope)
}

//TODO REMOVE
//将部分特定的查找方法作为扩展方法

//fun findRelatedLocalisationProperties(scriptPropertyName: String, project: Project, locale: ParadoxLocale? = null, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): List<ParadoxLocalisationProperty> {
//	return ParadoxLocalisationPropertyKeyIndex.filter(locale, project, GlobalSearchScope.allScope(project)) { name ->
//		isRelatedLocalisationPropertyName(name, scriptPropertyName)
//	}.sortedBy { it.name }
//}

//xxx, xxx_desc, xxx_effect_desc
//pop_job->job_, pop_category->pop_cat_

//private fun isRelatedLocalisationPropertyName(name: String, scriptPropertyName: String): Boolean {
//	val fqName = name.removePrefix("job_").removePrefix("pop_cat_")
//	return fqName == scriptPropertyName
//	       || fqName == scriptPropertyName + "_desc"
//	       || fqName == scriptPropertyName + "_effect_desc"
//}
//
//fun ParadoxLocalisationProperty.getRelatedLocalisationPropertyKey(): String {
//	val name = this.name
//	return when {
//		name.endsWith("_effect_desc") -> "paradox.documentation.effect"
//		name.endsWith("desc") -> "paradox.documentation.desc"
//		else -> "paradox.documentation.name"
//	}
//}

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
inline fun ParadoxLocalisationPropertyValue.renderRichText(): String {
	return ParadoxRichTextRenderer.render(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationPropertyValue.renderRichTextTo(buffer: StringBuilder) {
	ParadoxRichTextRenderer.renderTo(this, buffer)
}
