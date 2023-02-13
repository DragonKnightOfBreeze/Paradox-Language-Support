@file:Suppress("ComponentNotRegistered")

package icu.windea.pls.core.tool.actions

import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

/**
 * 用于在网页浏览器中打开一个链接。
 */
abstract class OpenUrlAction : DumbAwareAction(){
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
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        val targetUrl = getTargetUrl(fileInfo) ?: return //ignore
        BrowserUtil.open(targetUrl)
    }
    
    protected abstract fun isVisible(fileInfo: ParadoxFileInfo): Boolean
    
    protected abstract fun isEnabled(fileInfo: ParadoxFileInfo) : Boolean
    
    protected abstract fun getTargetUrl(fileInfo: ParadoxFileInfo): String?
}

class OpenModPageOnSteamWebsiteAction: OpenUrlAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo.rootType == ParadoxRootType.Mod
    }
    
    override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getRemoteId(fileInfo) != null
    }
    
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val remoteId = getRemoteId(fileInfo) ?: return null
        return getSteamWorkshopLink(remoteId)
    }
    
    private fun getRemoteId(fileInfo: ParadoxFileInfo): String? {
        return fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()?.descriptorInfo?.remoteFileId
    }
}

class OpenModPageOnSteamAction: OpenUrlAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo.rootType == ParadoxRootType.Mod
    }
    
    override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getRemoteId(fileInfo) != null
    }
    
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val remoteId = getRemoteId(fileInfo) ?: return null
        return getSteamWorkshopLinkOnSteam(remoteId)
    }
    
    private fun getRemoteId(fileInfo: ParadoxFileInfo): String? {
        return fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()?.descriptorInfo?.remoteFileId
    }
}