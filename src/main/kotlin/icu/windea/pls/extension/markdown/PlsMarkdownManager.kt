package icu.windea.pls.extension.markdown

import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.injection.ParadoxPathInjectionInfo
import icu.windea.pls.model.paths.ParadoxPath
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence

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
        val gameType = pathInfo.substringBefore(':', "").let { ParadoxGameType.get(it) }
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
