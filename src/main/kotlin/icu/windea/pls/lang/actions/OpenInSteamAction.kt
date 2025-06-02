package icu.windea.pls.lang.actions

import com.intellij.ide.*
import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

//com.intellij.ide.actions.RevealFileAction
//com.intellij.ide.actions.ShowFilePathAction

class OpenInSteamAction : DumbAwareAction() {
    //仅限游戏或模组的根目录

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        if (!RevealFileAction.isSupported()) return
        val virtualFile = getFile(e) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        e.presentation.isVisible = true
        e.presentation.isEnabled = isEnabled(fileInfo)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = getFile(e) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        val targetUrl = getTargetUrl(fileInfo) ?: return //ignore
        BrowserUtil.open(targetUrl)
    }

    private fun getFile(e: AnActionEvent): VirtualFile? {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val file = if (files == null || files.size == 1) e.getData(CommonDataKeys.VIRTUAL_FILE) else null
        if (file?.rootInfo == null) return null
        return file
    }

    private fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getTargetUrl(fileInfo) != null
    }

    private fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val rootInfo = fileInfo.rootInfo
        val steamId = rootInfo.steamId ?: return null
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsFacade.getDataProvider().getSteamGameStoreUrlInSteam(steamId)
            is ParadoxRootInfo.Mod -> PlsFacade.getDataProvider().getSteamWorkshopUrlInSteam(steamId)
        }
    }
}
