package icu.windea.pls.core.tool.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.nio.file.*

abstract class CopyUrlAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = false
        presentation.isEnabled = false
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        presentation.isVisible = isVisible(fileInfo)
        presentation.isEnabled = isEnabled(fileInfo)
        if(presentation.isVisible) {
            val targetUrl = getTargetUrl(fileInfo)
            if(targetUrl != null) {
                presentation.description = templatePresentation.description + " (" + targetUrl + ")"
            }
        }
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        val targetUrl = getTargetUrl(fileInfo) ?: return //ignore
        CopyPasteManager.getInstance().setContents(StringSelection(targetUrl.toString()))
    }
    
    protected open fun isVisible(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected open fun isEnabled(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected abstract fun getTargetUrl(fileInfo: ParadoxFileInfo): String?
}

class CopyGameStorePageUrlAction : CopyUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getSteamGameStoreLink(steamId)
    }
}

class CopyGameWorkshopPageUrlAction : CopyUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getSteamGameWorkshopLink(steamId)
    }
}

class CopyModPageUrlAction : CopyUrlAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo is ParadoxModRootInfo
    }
    
    override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getSteamId(fileInfo) != null
    }
    
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val steamId = getSteamId(fileInfo) ?: return null
        return getSteamWorkshopLink(steamId)
    }
    
    private fun getSteamId(fileInfo: ParadoxFileInfo): String? {
        return fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()?.descriptorInfo?.remoteFileId
    }
}