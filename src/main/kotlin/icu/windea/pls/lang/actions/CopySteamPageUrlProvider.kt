package icu.windea.pls.lang.actions

import com.intellij.ide.actions.DumbAwareCopyPathProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo

class CopySteamPageUrlProvider : DumbAwareCopyPathProvider() {
    // 仅限游戏或模组的根目录

    override fun getPathToElement(project: Project, virtualFile: VirtualFile?, editor: Editor?): String? {
        val fileInfo = virtualFile?.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (rootInfo.rootFile != virtualFile) return null
        return getTargetUrl(fileInfo)
    }

    private fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val rootInfo = fileInfo.rootInfo
        val steamId = rootInfo.steamId1 ?: return null
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsUrlService.getSteamGameStoreUrl(steamId)
            is ParadoxRootInfo.Mod -> PlsUrlService.getSteamWorkshopUrl(steamId)
            else -> null
        }
    }
}
