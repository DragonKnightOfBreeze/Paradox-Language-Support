package icu.windea.pls.extensions.markdown

import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.injection.ParadoxPathInjectionInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence

object MarkdownExtensionManager {
    object Keys : KeyRegistry() {
        val cachedPathInjectionInfo by registerKey<CachedValue<ParadoxPathInjectionInfo>>(Keys)
    }

    // region inline code

    fun getIdentifierFromInlineCode(element: PsiElement): String? {
        if (element.elementType != MarkdownElementTypes.CODE_SPAN) return null
        val idElement = element.firstChild?.nextSibling ?: return null
        if (idElement.prevSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        if (idElement.nextSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        // NOTE 3.1.0 accept any leaf token (e.g., `TEXT`, GFM `AT_PATH_TOKEN`), not just plain text
        return idElement.text
    }

    /**
     * 从 Markdown 内联代码 PSI 元素中提取目标名字信息。
     */
    fun resolveInfoFromInlineCode(element: PsiElement): MarkdownInlineCodeInfo? {
        val identifier = getIdentifierFromInlineCode(element) ?: return null
        val prefix = if (identifier.startsWith('@')) "@" else ""
        val name = if (prefix.isEmpty()) identifier else identifier.removePrefixOrNull(prefix) ?: return null
        if (name.isEmpty() || name.containsBlank()) return null

        val openingLength = element.firstChild?.textLength ?: return null
        val closingLength = element.lastChild?.textLength ?: return null
        val startOffset = openingLength + prefix.length
        val endOffset = element.textLength - closingLength
        if (startOffset > endOffset) return null

        return MarkdownInlineCodeInfo(prefix, name, TextRange(startOffset, endOffset))
    }

    /**
     * 将内联代码中的目标名字解析为对应的 PSI 元素。
     *
     * @param element Markdown 内联代码 PSI 元素，用于提供解析上下文。
     */
    fun resolveTargetFromInlineCode(element: PsiElement, prefix: String, name: String): PsiElement? {
        return when {
            prefix == "@" -> {
                if (!ParadoxNameValidators.checkScriptedVariableName(name)) return null
                val selector = ParadoxScriptedVariableSearch.selector(element.project, element).contextSensitive()
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).find()
            }
            prefix.isEmpty() -> {
                run {
                    val selector = ParadoxDefinitionSearch.selector(element.project, element).contextSensitive()
                    val result = ParadoxDefinitionSearch.searchElement(name, null, selector).find()
                    if (result != null) return result
                }
                if (!ParadoxNameValidators.checkLocalisationName(name)) return null
                val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                ParadoxLocalisationSearch.searchNormal(name, selector).find()
            }
            else -> null
        }
    }

    fun resolveTargetNameFromInlineCode(element: PsiElement): String {
        return when (element) {
            is ParadoxDefinitionElement -> element.definitionInfo?.name ?: element.name
            is ParadoxLocalisationProperty -> element.name
            is ParadoxScriptScriptedVariable -> element.name ?: ""
            else -> (element as? PsiNamedElement)?.name ?: ""
        }
    }

    // endregion

    // region injection

    fun getPathInjectionInfo(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPathInjectionInfo) {
            ProgressManager.checkCanceled()
            val value = resolvePathInjectionInfo(element)
            CachedValueProvider.Result.create(value, element)
        }
    }

    private fun resolvePathInjectionInfo(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        val fenceLanguage = element.fenceLanguage?.trim()
        if (fenceLanguage.isNullOrEmpty()) return null
        val infos = fenceLanguage.splitByBlank()
        if (infos.size < 2) return null

        val extraInfos = infos.drop(1)
        val pathInfo = extraInfos.firstNotNullOfOrNull { it.removePrefixOrNull("path=") }
        if (pathInfo.isNullOrEmpty()) return null
        val gameType = pathInfo.substringBefore(':', "").let { ParadoxGameType.getSpecific(it) }
        if (gameType == null) return null
        val path = pathInfo.substringAfter(':', "").trimFast('/')
        if (path.isEmpty()) return null

        val languageId = infos.firstOrNull()
        if (languageId.isNullOrEmpty()) return null
        val language = Language.getRegisteredLanguages().find { it.id.equals(languageId, ignoreCase = true) }
        if (language !is ParadoxLanguage) return null

        return ParadoxPathInjectionInfo(gameType, path)
    }

    fun getCodeFenceFromInjectedFile(injectedFile: PsiFile): MarkdownCodeFence? {
        val vFile = selectFile(injectedFile) ?: return null
        if (!VirtualFileService.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host == null) return null

        if (host !is MarkdownCodeFence) return null
        return host
    }

    fun getInjectedFileInfoFromInjectedFile(element: MarkdownCodeFence): ParadoxFileInfo? {
        val pathInjectionInfo = getPathInjectionInfo(element) ?: return null
        val path = ParadoxPath.resolve(pathInjectionInfo.path)
        if (!canInject(path)) return null

        run {
            val rootInfo = selectRootFile(element)?.rootInfo ?: return@run
            val group = ParadoxFileGroup.resolve(path)
            val injectedFileInfo = ParadoxFileInfo.Default(path.normalize(), "", group, rootInfo)
            return injectedFileInfo
        }

        // 需要尽可能兼容 markdown 文件不在游戏或模组目录中的情况

        val group = ParadoxFileGroup.resolve(path)
        val rootInfo = ParadoxRootInfo.Injected(gameType = pathInjectionInfo.gameType)
        val injectedFileInfo = ParadoxFileInfo.Default(path.normalize(), "", group, rootInfo)
        return injectedFileInfo
    }

    private fun canInject(path: ParadoxPath): Boolean {
        val fileExtension = path.fileExtension ?: return false
        return fileExtension in ChronicleConstants.scriptFileExtensions
            || fileExtension in ChronicleConstants.localisationFileExtensions
            || fileExtension in ChronicleConstants.csvFileExtensions
    }

    // endregion
}
