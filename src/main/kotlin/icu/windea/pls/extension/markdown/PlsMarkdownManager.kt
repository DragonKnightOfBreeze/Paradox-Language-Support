package icu.windea.pls.extension.markdown

import com.intellij.lang.*
import com.intellij.lang.injection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.injection.*
import org.intellij.plugins.markdown.lang.*
import org.intellij.plugins.markdown.lang.psi.impl.*

object PlsMarkdownManager {
    object Keys : KeyRegistry() {
        val cachedPathInfo by createKey<CachedValue<ParadoxPathInjectionInfo>>(Keys)
    }

    fun getIdentifierFromInlineCode(element: PsiElement): String? {
        if (element.elementType != MarkdownElementTypes.CODE_SPAN) return null
        val idElement = element.firstChild?.nextSibling ?: return null
        if (idElement.elementType != MarkdownTokenTypes.TEXT) return null
        if (idElement.prevSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        if (idElement.nextSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        val text = idElement.text
        return text
    }

    fun getPathInfo(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        return doGetPathInfoFromCache(element)
    }

    private fun doGetPathInfoFromCache(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPathInfo) {
            val value = doGetPathInfo(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetPathInfo(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        val fenceLanguage = element.fenceLanguage?.trim()
        if (fenceLanguage.isNullOrEmpty()) return null
        val infos = fenceLanguage.splitByBlank()
        if (infos.size < 2) return null

        val extraInfos = infos.drop(1)
        val pathInfo = extraInfos.firstNotNullOfOrNull { it.removePrefixOrNull("path=") }
        if (pathInfo.isNullOrEmpty()) return null
        val gameType = pathInfo.substringBefore(':', "").let { ParadoxGameType.resolve(it) }
        if (gameType == null) return null
        val path = pathInfo.substringAfter(':', "").trimFast('/')
        if (path.isEmpty()) return null

        val languageId = infos.firstOrNull()
        if (languageId.isNullOrEmpty()) return null
        val language = Language.getRegisteredLanguages().find { it.id.equals(languageId, ignoreCase = true) }
        if (language !is ParadoxBaseLanguage) return null

        return ParadoxPathInjectionInfo(gameType, path)
    }

    fun getCodeFenceFromInjectedFile(injectedFile: PsiFile): MarkdownCodeFence? {
        val vFile = selectFile(injectedFile) ?: return null
        if (!PlsVfsManager.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host == null) return null

        if (host !is MarkdownCodeFence) return null
        return host
    }

    fun getInjectFileInfoFromInjectedFile(element: MarkdownCodeFence): ParadoxFileInfo? {
        val pathInfo = getPathInfo(element) ?: return null
        val path = ParadoxPath.resolve(pathInfo.path)
        if (!canInject(path)) return null

        run {
            val rootInfo = selectRootFile(element)?.rootInfo
            if (rootInfo == null) return@run
            val fileType = ParadoxFileType.resolve(path)
            val injectedFileInfo = ParadoxFileInfo(path, "", fileType, rootInfo)
            return injectedFileInfo
        }

        //需要尽可能兼容markdown文件不在游戏或模组目录中的情况

        val rootInfo = ParadoxRootInfo.Injected(pathInfo.gameType)
        val fileType = ParadoxFileType.resolve(path)
        val injectedFileInfo = ParadoxFileInfo(path, "", fileType, rootInfo)
        return injectedFileInfo
    }

    private fun canInject(path: ParadoxPath): Boolean {
        val fileExtension = path.fileExtension ?: return false
        return fileExtension in PlsConstants.scriptFileExtensions
            || fileExtension in PlsConstants.localisationFileExtensions
            || fileExtension in PlsConstants.csvFileExtensions
    }
}
