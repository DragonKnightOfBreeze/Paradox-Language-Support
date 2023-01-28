@file:Suppress("unused")

package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import icu.windea.pls.tool.*
import java.lang.Integer.*
import java.util.*

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun getSettings() = service<ParadoxSettings>().state

fun getCwtConfig(project: Project = getTheOnlyOpenOrDefaultProject()) = project.service<CwtConfigProvider>().configGroups

fun preferredParadoxLocale(): CwtLocalisationLocaleConfig? {
	val primaryLocale = getSettings().preferredLocale.orEmpty()
	if(primaryLocale.isNotEmpty() && primaryLocale != "auto") {
		val locales = getCwtConfig().core.localisationLocales
		val usedLocale = locales.get(primaryLocale)
		if(usedLocale != null) return usedLocale
	}
	//基于OS得到对应的语言区域，或者使用英文
	val userLanguage = System.getProperty("user.language") ?: "en"
	val localesByCode = getCwtConfig().core.localisationLocalesByCode
	return localesByCode.get(userLanguage) ?: localesByCode.get("en")
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

fun Language.isParadoxLanguage() = this.isKindOf(ParadoxScriptLanguage) || this.isKindOf(ParadoxLocalisationLanguage)

fun PsiReference.canResolveParameter(): Boolean {
	return when(this) {
		is ParadoxScriptExpressionPsiReference -> this.isKey
		is ParadoxParameterPsiReference -> true
		is ParadoxArgumentPsiReference -> true
		is ParadoxScriptValueParameterExpressionNode.Reference -> true
		is ParadoxLocalisationProperty -> true //can, but not supported yet
		else -> false
	}
}

fun PsiReference.canResolveValueSetValue(): Boolean {
	return when(this) {
		is ParadoxScriptExpressionPsiReference -> true
		is ParadoxValueSetValueExpressionNode.Reference -> true
		is ParadoxDataExpressionNode.Reference -> true
		is ParadoxTemplateExpressionNode.Reference -> true
		is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
		is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
		else -> false
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

val PsiElement.localeConfig: CwtLocalisationLocaleConfig?
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
	get() = ParadoxCoreHandler.getFileInfo(this)
val PsiFile.fileInfo: ParadoxFileInfo?
	get() = ParadoxCoreHandler.getFileInfo(this)
val PsiElement.fileInfo: ParadoxFileInfo?
	get() = ParadoxCoreHandler.getFileInfo(this)

val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo?
	get() = ParadoxDefinitionHandler.getInfo(this)

val ParadoxScriptMemberElement.definitionMemberInfo: ParadoxDefinitionMemberInfo?
	get() = ParadoxDefinitionMemberHandler.getInfo(this)

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
	get() = ParadoxLocalisationHandler.getInfo(this)

val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	get() = ParadoxComplexEnumValueHandler.getInfo(this)

val ParadoxLocalisationLocale.localeConfig: CwtLocalisationLocaleConfig?
	get() = getCwtConfig(project).core.localisationLocales.get(name)

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxTextColorInfo?
	get() {
		//大写或小写字母，不限定位置
		val colorId = this.propertyReferenceParameter?.text?.find { it.isExactLetter() } ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
			?: return null
		return ParadoxTextColorHandler.getTextColorInfo(colorId.toString(), gameType, project)
	}

val ParadoxLocalisationColorfulText.colorConfig: ParadoxTextColorInfo?
	get() {
		val colorId = this.name ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType 
			?: return null
		return ParadoxTextColorHandler.getTextColorInfo(colorId, gameType, project)
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

private fun resolveCwtLink(linkWithoutPrefix: String, sourceElement: PsiElement): PsiElement? {
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
		"enums" -> {
			val name = tokens.getOrNull(2) ?: return null
			val valueName = tokens.getOrNull(3)
			val config = getCwtConfig(project).getValue(gameType).enums[name] ?: return null
			if(valueName == null) return config.pointer.element
			return config.valueConfigMap.get(valueName)?.pointer?.element
		}
		"complex_enums" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).complexEnums[name] ?: return null
			return config.pointer.element
		}
		"values" -> {
			val name = tokens.getOrNull(2) ?: return null
			val valueName = tokens.getOrNull(3)
			val config = getCwtConfig(project).getValue(gameType).values[name] ?: return null
			if(valueName == null) return config.pointer.element
			return config.valueConfigMap.get(valueName)?.pointer?.element
		}
		"scopes" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).scopeAliasMap[name] ?: return null
			return config.pointer.element
		}
		"system_links" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).systemLinks[name] ?: return null
			return config.pointer.element
		}
		"links" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).links[name] ?: return null
			return config.pointer.element
		}
		"modifier_categories" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).modifierCategories[name] ?: return null
			return config.pointer.element
		}
		"modifiers" -> {
			val name = tokens.getOrNull(2) ?: return null
			val config = getCwtConfig(project).getValue(gameType).modifiers[name] ?: return null
			return config.pointer.element
		}
		else -> null
	}
}

private fun resolveDefinitionLink(linkWithoutPrefix: String, sourceElement: PsiElement): ParadoxScriptDefinitionElement? {
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
	return ParadoxLocalisationSearch.search(name, project, selector = selector).find()
}

private fun resolveFilePathLink(linkWithoutPrefix: String, sourceElement: PsiElement): PsiFile? {
	ProgressManager.checkCanceled()
	val tokens = linkWithoutPrefix.split('/', limit = 2)
	val gameType = tokens.getOrNull(0)?.let { ParadoxGameType.resolve(it) } ?: return null
	val filePath = tokens.getOrNull(1) ?: return null
	val project = sourceElement.project
	val selector = fileSelector().gameType(gameType).preferRootFrom(sourceElement)
	return ParadoxFilePathSearch.search(filePath, project, selector = selector).find()
		?.toPsiFile(project)
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
	val isResolved = resolved == true || (resolved == null && ParadoxLocalisationSearch.search(name, context.project, selector = localisationSelector().gameTypeFrom(context)).findFirst() != null)
	if(isResolved) return appendPsiLink("$localisationLinkPrefix${gameType.id}/$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendFilePathLink(gameType: ParadoxGameType, filePath: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果可以定位到绝对路径，则显示链接
	val isResolved = resolved == true || (resolved == null && ParadoxFilePathSearch.search(filePath, context.project, selector = fileSelector().gameTypeFrom(context)).findFirst() != null)
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
			appendLink(rootUri, PlsDocBundle.message("text.localLinkLabel"))
			if(remoteFileId != null) {
				append(" | ")
				appendLink(getSteamWorkshopLinkOnSteam(remoteFileId), PlsDocBundle.message("text.steamLinkLabel"))
				appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
				append(" | ")
				appendLink(getSteamWorkshopLink(remoteFileId), PlsDocBundle.message("text.steamWebsiteLinkLabel")) //自带外部链接图标
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

fun StringBuilder.appendIndent(): StringBuilder {
	return append("&nbsp;&nbsp;&nbsp;&nbsp;")
}
//endregion
