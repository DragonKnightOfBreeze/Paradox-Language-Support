@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.lang.Integer.*
import java.util.*

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun getSettings() = service<ParadoxSettings>()

fun getInternalConfig(project: Project? = null) = (project ?: getTheOnlyOpenOrDefaultProject()).service<InternalConfigProvider>().configGroup

fun getCwtConfig(project: Project) = project.service<CwtConfigProvider>().configGroups

fun preferredParadoxLocale(): ParadoxLocaleConfig? {
	val primaryLocale = getSettings().localisationPreferredLocale.orEmpty()
	if(primaryLocale.isNotEmpty() && primaryLocale != "auto") {
		val usedLocale = InternalConfigHandler.getLocale(primaryLocale)
		if(usedLocale != null) return usedLocale
	}
	//基于OS得到对应的语言区域，或者使用英文
	val userLanguage = System.getProperty("user.language")
	return InternalConfigHandler.getLocaleByCode(userLanguage) ?: InternalConfigHandler.getLocaleByCode("en")
}

/**
 * 比较游戏版本。允许通配符，如："3.3.*"
 */
infix fun String.compareGameVersion(otherVersion: String): Int {
	val versionSnippets = this.split('.')
	val otherVersionSnippets = otherVersion.split('.')
	val minSnippetSize = min(versionSnippets.size, otherVersionSnippets.size)
	for(i in 0 until minSnippetSize) {
		val versionSnippet = versionSnippets[i]
		val otherVersionSnippet = otherVersionSnippets[i]
		if(versionSnippet == otherVersionSnippet || versionSnippet == "*" || otherVersion == "*") continue
		return versionSnippet.compareTo(otherVersionSnippet)
	}
	return 0
}
//endregion

//region Core Extensions
fun resolveRootInfo(rootFile: VirtualFile, canBeNotAvailable: Boolean = true): ParadoxRootInfo? {
	val rootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
	if(rootInfo != null && (canBeNotAvailable || rootInfo.isAvailable)) {
		ParadoxRootInfo.values.add(rootInfo)
		return rootInfo
	}
	ParadoxRootInfo.values.remove(rootInfo)
	val resolvedRootInfo = doResolveRootInfo(rootFile, canBeNotAvailable)
	runCatching {
		rootFile.putUserData(PlsKeys.rootInfoKey, resolvedRootInfo)
	}
	if(resolvedRootInfo != null) {
		ParadoxRootInfo.values.add(resolvedRootInfo)
	}
	return resolvedRootInfo
}

private fun doResolveRootInfo(rootFile: VirtualFile, canBeNotAvailable: Boolean): ParadoxRootInfo? {
	if(rootFile is StubVirtualFile || !rootFile.isValid) return null
	if(!rootFile.isDirectory) return null
	
	var rootType: ParadoxRootType? = null
	var descriptorFile: VirtualFile? = null
	var markerFile: VirtualFile? = null
	val rootName = rootFile.nameWithoutExtension //忽略扩展名
	when {
		rootName == ParadoxRootType.PdxLauncher.id -> {
			rootType = ParadoxRootType.PdxLauncher
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(PlsConstants.launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		rootName == ParadoxRootType.PdxOnlineAssets.id -> {
			rootType = ParadoxRootType.PdxOnlineAssets
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(PlsConstants.launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		rootName == ParadoxRootType.TweakerGuiAssets.id -> {
			rootType = ParadoxRootType.TweakerGuiAssets
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(PlsConstants.launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		else -> {
			for(rootChild in rootFile.children) {
				if(rootChild.isDirectory) continue
				if(!canBeNotAvailable && !rootChild.isValid) continue
				val rootChildName = rootChild.name
				when {
					rootChildName.equals(PlsConstants.launcherSettingsFileName, true) -> {
						rootType = ParadoxRootType.Game
						descriptorFile = rootChild
						markerFile = rootChild
						break
					}
					rootChildName.equals(PlsConstants.descriptorFileName, true) -> {
						rootType = ParadoxRootType.Mod
						descriptorFile = rootChild
						if(descriptorFile != null && markerFile != null) break
					}
					ParadoxGameType.resolve(rootChild) != null -> {
						markerFile = rootChild
						if(descriptorFile != null && markerFile != null) break
					}
				}
			}
		}
	}
	if(descriptorFile != null && rootType != null) {
		return ParadoxRootInfo(rootFile, descriptorFile, markerFile, rootType)
	}
	return null
}

fun resolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
	val resolvedFileInfo = doResolveFileInfo(file)
	runCatching {
		file.putUserData(PlsKeys.fileInfoKey, resolvedFileInfo)
	}
	return resolvedFileInfo
}

fun doResolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
	if(file is StubVirtualFile || !file.isValid) return null
	val fileName = file.name
	val subPaths = LinkedList<String>()
	subPaths.addFirst(fileName)
	var currentFile: VirtualFile? = file.parent
	while(currentFile != null) {
		val rootInfo = resolveRootInfo(currentFile, false)
		if(rootInfo != null) {
			val path = ParadoxPath.resolve(subPaths)
			val fileType = ParadoxFileType.resolve(file, rootInfo.gameType, path)
			val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootInfo)
			file.putUserData(PlsKeys.fileInfoKey, fileInfo)
			return fileInfo
		}
		subPaths.addFirst(currentFile.name)
		currentFile = currentFile.parent
	}
	return null
}

fun reparseFilesInRoot(rootFile: VirtualFile) {
	//重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
	try {
		FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Root of paradox files $rootFile changed.") { }
	} catch(e: Exception) {
		//ignore
	} finally {
		//要求重新索引
		FileBasedIndex.getInstance().requestReindex(rootFile)
		//要求重建缓存（CachedValue）
		ParadoxGameTypeModificationTracker.fromRoot(rootFile).increment()
	}
}

fun reparseScriptFiles() {
	try {
		FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Ignored file name of paradox script files changed.") { }
	} catch(e: Exception) {
		//ignore
	} finally {
		//要求重新索引
		for(rootInfo in ParadoxRootInfo.values) {
			FileBasedIndex.getInstance().requestReindex(rootInfo.rootFile)
		}
	}
}
//endregion

//region VFS Extensions
/**
 * 当前[VirtualFile]的内容文件。（缓存且仍然存在的文件，首个子文件，生成的子文件，或者自身）
 */
var VirtualFile.contentFile
	get() = getUserData(PlsKeys.contentFileKey)?.takeIf { it.exists() }
		?: this.children.firstOrNull()
		?: ParadoxFileLocator.getGeneratedFileName(this)
		?: this
	set(value) = putUserData(PlsKeys.contentFileKey, value)
//endregion

//region PsiElement Extensions
fun PsiElement.useAllUseScope(): Boolean {
	if(this is PsiFile) {
		if(this.fileInfo != null) return true
	}
	val language = this.language
	return language == CwtLanguage || language == ParadoxScriptLanguage || language == ParadoxLocalisationLanguage
}

fun PsiElement.isQuoted(): Boolean {
	return text.isQuoted()
}

val PsiElement.localeConfig: ParadoxLocaleConfig?
	get() {
		if(this.language == ParadoxLocalisationLanguage) {
			var current = this
			while(true) {
				when {
					current is ParadoxLocalisationFile -> return current.locale?.localeConfig
					current is ParadoxLocalisationPropertyList -> return current.locale.localeConfig
					current is ParadoxLocalisationLocale -> return current.localeConfig
					current is PsiFile -> return preferredParadoxLocale() //不期望的结果
				}
				current = current.parent ?: break
			}
			return current.containingFile.localeConfig
		} else {
			return preferredParadoxLocale()
		}
	}

//注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
//如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

val VirtualFile.fileInfo: ParadoxFileInfo?
	get() = this.getUserDataOnValid(PlsKeys.fileInfoKey) { it.isValid }
val PsiFile.fileInfo: ParadoxFileInfo?
	get() = this.originalFile.virtualFile?.fileInfo //需要使用原始文件
val PsiElement.fileInfo: ParadoxFileInfo?
	get() = this.containingFile?.fileInfo

val ParadoxDefinitionProperty.definitionInfo: ParadoxDefinitionInfo?
	get() = ParadoxDefinitionInfoHandler.get(this)

val ParadoxDefinitionProperty.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptPropertyKey.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptValue.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptExpressionElement.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
	get() = ParadoxLocalisationInfoHandler.get(this)

val ParadoxScriptExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	get() = ParadoxComplexEnumValueInfoHandler.get(this)

val ParadoxLocalisationLocale.localeConfig: ParadoxLocaleConfig?
	get() = InternalConfigHandler.getLocale(name, project)

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxTextColorConfig?
	get() {
		//大写或小写字母，不限定位置
		val colorId = this.propertyReferenceParameter?.text?.find { it.isExactLetter() } ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
			?: return null
		return DefinitionConfigHandler.getTextColorConfig(colorId.toString(), gameType, project)
	}

val ParadoxLocalisationColorfulText.colorConfig: ParadoxTextColorConfig?
	get() {
		val colorId = this.name ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType 
			?: return null
		return DefinitionConfigHandler.getTextColorConfig(colorId, gameType, project)
	}

fun ParadoxScriptValue.isNullLike(): Boolean {
	return when {
		this is ParadoxScriptBlock -> this.isEmpty
		this is ParadoxScriptString -> this.textMatches("")
		this is ParadoxScriptInt -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptFloat -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptBoolean -> this.textMatches("no")
		else -> false
	}
}
//endregion

//region Find Extensions
/**
 * 基于本地化名字索引，根据名字查找本地化（localisation）。
 */
fun findLocalisation(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.findOne(name, project, scope, preferFirst, selector)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找所有的本地化（localisation）。
 * @see preferredParadoxLocale
 */
fun findLocalisations(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): Set<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAll(name, project, scope, selector)
}

/**
 * 基于本地化名字索引，根据关键字和推断的语言区域遍历所有的本地化（localisation）。对本地化的键去重。
 * @see preferredParadoxLocale
 */
inline fun processLocalisationVariants(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector(),
	crossinline processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
): Boolean {
	val maxSize = getSettings().maxCompleteSize
	return ParadoxLocalisationNameIndex.processVariants(keyword, project, scope, maxSize, selector, processor)
}

/**
 * 基于同步本地化名字索引，根据名字查找同步本地化（localisation_synced）。
 */
fun findSyncedLocalisation(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): ParadoxLocalisationProperty? {
	return ParadoxSyncedLocalisationNameIndex.findOne(name, project, scope, preferFirst, selector)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找所有的同步本地化（localisation_synced）。
 * @see preferredParadoxLocale
 */
fun findSyncedLocalisations(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): Set<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findAll(name, project, scope, selector)
}

/**
 * 基于同步本地化名字索引，根据关键字和推断的语言区域遍历所有的同步本地化（synced_localisation）。对本地化的键去重。
 * @see preferredParadoxLocale
 */
inline fun processSyncedLocalisationVariants(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector(),
	crossinline processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
): Boolean {
	val maxSize = getSettings().maxCompleteSize
	return ParadoxLocalisationNameIndex.processVariants(keyword, project, scope, maxSize, selector, processor)
}


/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。 默认为`true`。
 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
 */
fun findFileByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionTypes.Exact,
	ignoreCase: Boolean = true,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): VirtualFile? {
	return ParadoxFilePathIndex.findOne(filePath, scope, expressionType, ignoreCase, selector)
}

/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找所有匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
 */
fun findFilesByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionTypes.Exact,
	ignoreCase: Boolean = true,
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(filePath, scope, expressionType, ignoreCase, distinct, selector)
}

/**
 * 基于文件索引，根据相查找所有匹配的（位于游戏或模组根目录或其子目录中的）文件（非目录）。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
 */
fun findAllFilesByFilePath(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	ignoreCase: Boolean = true,
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(project, scope, ignoreCase, distinct, selector)
}
//endregion

//region Psi Link Extensions
private const val cwtLinkPrefix = "#cwt/"
private const val definitionLinkPrefix = "#definition/"
private const val localisationLinkPrefix = "#localisation/"
private const val filePathLinkPrefix = "#path/"

fun resolveLink(link: String, sourceElement: PsiElement): PsiElement? {
	//e.g. #cwt/stellaris/types/civic_or_origin/civic
	link.removePrefixOrNull(cwtLinkPrefix)?.let { return resolveCwtLink(it, sourceElement) }
	//e.g. #definition/stellaris/civic_or_origin.origin/origin_default
	link.removePrefixOrNull(definitionLinkPrefix)?.let { return resolveDefinitionLink(it, sourceElement) }
	//e.g. #localisation/stellaris/KEY
	link.removePrefixOrNull(localisationLinkPrefix)?.let { return resolveLocalisationLink(it, sourceElement) }
	//e.g. #path/stellaris/path
	link.removePrefixOrNull(filePathLinkPrefix)?.let { return resolveFilePathLink(it, sourceElement) }
	return null
}

private fun resolveCwtLink(linkWithoutPrefix: String, sourceElement: PsiElement): CwtProperty? {
	ProgressManager.checkCanceled()
	val tokens = linkWithoutPrefix.split('/')
	if(tokens.size > 4) return null
	val gameType = tokens.getOrNull(0) ?: return null
	val category = tokens.getOrNull(1) ?: return null
	val project = sourceElement.project
	return when(category) {
		"types" -> {
			val name = tokens.getOrNull(2)
			val subtypeName = tokens.getOrNull(3)
			val config = when {
				name == null -> null
				subtypeName == null -> getCwtConfig(project).getValue(gameType).types[name]
				else -> getCwtConfig(project).getValue(gameType).types.getValue(name).subtypes[subtypeName]
			} ?: return null
			return config.pointer.element
		}
		"scopes" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).scopeAliasMap[name] ?: return null
			return config.pointer.element
		}
		"enums" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).enums[name] ?: return null
			return config.pointer.element
		}
		"complex_enums" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).complexEnums[name] ?: return null
			return config.pointer.element
		}
		"values" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).values[name] ?: return null
			return config.pointer.element
		}
		else -> null
	}
}

private fun resolveDefinitionLink(linkWithoutPrefix: String, sourceElement: PsiElement): ParadoxDefinitionProperty? {
	ProgressManager.checkCanceled()
	val tokens = linkWithoutPrefix.split('/')
	if(tokens.size > 3) return null
	val gameType = tokens.getOrNull(0)?.let { ParadoxGameType.resolve(it) } ?: return null
	val typeExpression = tokens.getOrNull(1) ?: return null
	val name = tokens.getOrNull(2) ?: return null
	val project = sourceElement.project
	val selector = definitionSelector().gameType(gameType).preferRootFrom(sourceElement)
	return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
}

private fun resolveLocalisationLink(linkWithoutPrefix: String, sourceElement: PsiElement): ParadoxLocalisationProperty? {
	ProgressManager.checkCanceled()
	val tokens = linkWithoutPrefix.split('/')
	if(tokens.size > 2) return null
	val gameType = tokens.getOrNull(0)?.let { ParadoxGameType.resolve(it) } ?: return null
	val name = tokens.getOrNull(1) ?: return null
	val project = sourceElement.project
	val selector = localisationSelector().gameType(gameType).preferRootFrom(sourceElement).preferLocale(sourceElement.localeConfig)
	return findLocalisation(name, project, selector = selector)
}

private fun resolveFilePathLink(linkWithoutPrefix: String, sourceElement: PsiElement): PsiFile? {
	ProgressManager.checkCanceled()
	val tokens = linkWithoutPrefix.split('/', limit = 2)
	val gameType = tokens.getOrNull(0)?.let { ParadoxGameType.resolve(it) } ?: return null
	val filePath = tokens.getOrNull(1) ?: return null
	val project = sourceElement.project
	val selector = fileSelector().gameType(gameType).preferRootFrom(sourceElement)
	return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
}
//endregion

//region Documentation Extensions
fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
	if(condition) append(text)
	return this
}

fun StringBuilder.appendExternalLinkIcon(): StringBuilder {
	append("<icon src='ide/external_link_arrow.svg'/>")
	return this
}

fun StringBuilder.appendUnresolvedLink(label: String): StringBuilder {
	append(label.escapeXml()) //直接显示对应的标签文本
	return this
}

fun StringBuilder.appendLink(refText: String, label: String): StringBuilder {
	append("<a href=\"").append(refText).append("\">").append(label).append("</a>")
	return this
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): StringBuilder {
	DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
	return this
}

fun StringBuilder.appendCwtLink(name: String, link: String, context: PsiElement? = null): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(PlsConstants.unresolvedString)
	//如果可以被解析为CWT规则，则显示链接（context传null时总是显示）
	val isResolved = context == null || resolveCwtLink(link, context) != null
	if(isResolved) return appendPsiLink("$cwtLinkPrefix$link".escapeXml(), name.escapeXml())
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendDefinitionLink(gameType: ParadoxGameType, name: String, typeExpression: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(PlsConstants.unresolvedString)
	//如果可以被解析为定义，则显示链接
	val isResolved = resolved == true || (resolved == null && ParadoxDefinitionSearch.search(name, null, context.project, selector = definitionSelector().gameTypeFrom(context)).findFirst() != null)
	if(isResolved) return appendPsiLink("$definitionLinkPrefix${gameType.id}/$typeExpression/$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendLocalisationLink(gameType: ParadoxGameType, name: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(PlsConstants.unresolvedString)
	//如果可以被解析为本地化，则显示链接
	val isResolved = resolved == true || (resolved == null && findLocalisation(name, context.project, preferFirst = true, selector = localisationSelector().gameTypeFrom(context)) != null)
	if(isResolved) return appendPsiLink("$localisationLinkPrefix${gameType.id}/$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendFilePathLink(gameType: ParadoxGameType, filePath: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果可以定位到绝对路径，则显示链接
	val isResolved = resolved == true || (resolved == null && findFileByFilePath(filePath, context.project, selector = fileSelector().gameTypeFrom(context)) != null)
	if(isResolved) return appendPsiLink("$filePathLinkPrefix${gameType.id}/$filePath", filePath)
	//否则显示未解析的链接
	return appendUnresolvedLink(filePath)
}

/**
 * @param local 输入的[url]是否是本地绝对路径。
 */
fun StringBuilder.appendImgTag(url: String, local: Boolean = true): StringBuilder {
	append("<img src=\"")
	if(local) append(url.toFileUrl()) else append(url)
	append("\" />")
	return this
}

fun StringBuilder.appendImgTag(url: String, width: Int, height: Int, local: Boolean = true): StringBuilder {
	append("<img src=\"")
	if(local) append(url.toFileUrl()) else append(url)
	//这里不能使用style="..."
	append("\" width=\"").append(width).append("\" height=\"").append(height)
	append(" vspace=\"0\" hspace=\"0\"")
	append("\"/>")
	return this
}

fun StringBuilder.appendFileInfoHeader(fileInfo: ParadoxFileInfo?): StringBuilder {
	if(fileInfo != null) {
		append("<span>")
		//描述符信息（模组名、版本等）
		append("[").append(fileInfo.rootInfo.gameType.description).append(" ").append(fileInfo.rootType.description)
		val descriptorInfo = fileInfo.descriptorInfo
		if(descriptorInfo != null) {
			if(fileInfo.rootType == ParadoxRootType.Mod) {
				append(": ").append(descriptorInfo.name.escapeXml())
			}
			val version = descriptorInfo.version
			if(version != null) append("@").append(version)
		}
		append("]")
		grayed {
			val remoteFileId = descriptorInfo?.remoteFileId
			//remoteFileId（暂不显示）
			//if(remoteFileId != null) {
			//	append(" ").append(PlsDocBundle.message("name.core.remoteFileId")).append(": ").append(remoteFileId).append(" )
			//}
			//相关链接
			val rootUri = fileInfo.rootPath.toUri().toString() //通过这种方式获取需要的url
			append(" ")
			appendLink(rootUri, PlsDocBundle.message("name.core.localLinkLabel"))
			if(remoteFileId != null) {
				append(" | ")
				appendLink(getSteamWorkshopLinkOnSteam(remoteFileId), PlsDocBundle.message("name.core.steamLinkLabel"))
				appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
				append(" | ")
				appendLink(getSteamWorkshopLink(remoteFileId), PlsDocBundle.message("name.core.steamWebsiteLinkLabel")) //自带外部链接图标
			}
		}
		append("</span>")
		appendBr()
		//文件信息（相对于游戏或模组根目录的路径）
		append("[").append(fileInfo.path).append("]")
		appendBr()
	}
	return this
}

fun StringBuilder.appendBr(): StringBuilder {
	return append("<br>")
}
//endregion