package icu.windea.pls.lang.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

class CopySteamPageProvider : DumbAwareCopyPathProvider() {
    //仅限游戏或模组的根目录

    override fun getPathToElement(project: Project, virtualFile: VirtualFile?, editor: Editor?): String? {
        val fileInfo = virtualFile?.fileInfo ?: return null
        if (fileInfo.rootInfo.rootFile != virtualFile) return null
        return getTargetUrl(fileInfo)
    }

    private fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val rootInfo = fileInfo.rootInfo
        val steamId = rootInfo.steamId ?: return null
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsFacade.getDataProvider().getSteamGameStoreUrl(steamId)
            is ParadoxRootInfo.Mod -> PlsFacade.getDataProvider().getSteamWorkshopUrl(steamId)
        }
    }
}
