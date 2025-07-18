package icu.windea.pls.extension.markdown

import com.intellij.lang.*
import com.intellij.lang.injection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.injection.*
import org.intellij.plugins.markdown.lang.*
import org.intellij.plugins.markdown.lang.psi.impl.*

object PlsMarkdownManager {
    object Keys : KeyRegistry() {
        val cachedPathInfo by createKey<CachedValue<ParadoxPathInjectionInfo>>(this)
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
        return doGetPathInfo(element)
    }

    private fun doGetPathInfoFromCache(element: MarkdownCodeFence): ParadoxPathInjectionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPathInfo) {
            val value = doGetPathInfo(element)
            CachedValueProvider.Result.create(value, element)
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
        if (!PlsFileManager.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host == null) return null

        if (host !is MarkdownCodeFence) return null
        return host
    }

    fun getInjectFileInfoFromInjectedFile(injectedFile: PsiFile, element: MarkdownCodeFence): ParadoxFileInfo? {
        val pathInfo = getPathInfo(element) ?: return null
        val path = ParadoxPath.resolve(pathInfo.path)
        val fileExtension = path.fileExtension ?: return null
        if (fileExtension !in PlsConstants.scriptFileExtensions && fileExtension !in PlsConstants.localisationFileExtensions) return null

        run {
            val rootInfo = selectRootFile(element)?.rootInfo
            if (rootInfo == null) return@run
            val fileType = ParadoxFileType.resolve(path, rootInfo)
            val injectedFileInfo = ParadoxFileInfo(path, "", fileType, rootInfo)
            return injectedFileInfo
        }

        //需要尽可能兼容markdown文件不在游戏或模组目录中的情况

        val rootInfo = ParadoxRootInfo.Injected(pathInfo.gameType)
        val fileType = ParadoxFileType.resolve(path, rootInfo)
        val injectedFileInfo = ParadoxFileInfo(path, "", fileType, rootInfo)
        return injectedFileInfo
    }
}
