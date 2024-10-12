package icu.windea.pls.lang.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

class CopySteamPageProvider: DumbAwareCopyPathProvider() {
    //仅限游戏或模组的根目录
    
    override fun getPathToElement(project: Project, virtualFile: VirtualFile?, editor: Editor?): String? {
        val fileInfo = virtualFile?.fileInfo ?: return null
        if(fileInfo.rootInfo.rootFile != virtualFile) return null
        return getTargetUrl(fileInfo)
    }
    
    private fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val rootInfo = fileInfo.rootInfo
        return when {
            rootInfo is ParadoxGameRootInfo -> {
                val steamId = rootInfo.gameType.steamId
                getDataProvider().getSteamGameStoreUrl(steamId)
            }
            rootInfo is ParadoxModRootInfo -> {
                val steamId = rootInfo.descriptorInfo.remoteFileId ?: return null
                getDataProvider().getSteamWorkshopUrl(steamId)
            }
            else -> null
        }
    }
}
