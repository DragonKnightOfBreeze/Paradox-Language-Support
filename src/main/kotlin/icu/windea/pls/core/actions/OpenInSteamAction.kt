package icu.windea.pls.core.actions

import com.intellij.ide.*
import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

//com.intellij.ide.actions.RevealFileAction
//com.intellij.ide.actions.ShowFilePathAction

class OpenInSteamAction : DumbAwareAction() {
    //仅限游戏或模组的根目录
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        if(!RevealFileAction.isSupported()) return
        val virtualFile = getFile(e) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        e.presentation.isVisible = true
        e.presentation.isEnabled = isEnabled(fileInfo)
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = getFile(e) ?: return
        val fileInfo  = virtualFile.fileInfo ?: return
        val targetUrl = getTargetUrl(fileInfo) ?: return //ignore
        BrowserUtil.open(targetUrl)
    }
    
    private fun getFile(e: AnActionEvent): VirtualFile? {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val file = if(files == null || files.size == 1) e.getData(CommonDataKeys.VIRTUAL_FILE) else null
        if(file?.rootInfo == null) return null
        return file
    }
    
    private fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getTargetUrl(fileInfo) != null
    }
    
    private fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val rootInfo = fileInfo.rootInfo
        return when {
            rootInfo is ParadoxGameRootInfo -> {
                val steamId = rootInfo.gameType.steamId
                getSteamGameStoreLinkInSteam(steamId)
            }
            rootInfo is ParadoxModRootInfo -> {
                val steamId = rootInfo.descriptorInfo.remoteFileId ?: return null
                getSteamWorkshopLinkInSteam(steamId)
            }
            else -> null
        }
    }
}
