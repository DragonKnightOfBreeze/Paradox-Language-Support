package icu.windea.pls.tools.actions

import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

abstract class OpenUrlAction : DumbAwareAction() {
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
        if (presentation.isVisible) {
            val targetUrl = getTargetUrl(fileInfo)
            if (targetUrl != null) {
                presentation.description = templatePresentation.description + " (" + targetUrl + ")"
            }
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        val targetUrl = getTargetUrl(fileInfo) ?: return //ignore
        BrowserUtil.open(targetUrl)
    }

    protected open fun isVisible(fileInfo: ParadoxFileInfo): Boolean = true

    protected open fun isEnabled(fileInfo: ParadoxFileInfo): Boolean = true

    protected abstract fun getTargetUrl(fileInfo: ParadoxFileInfo): String?
}

class OpenGameStorePageInSteamAction : OpenUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getDataProvider().getSteamGameStoreUrlInSteam(steamId)
    }
}

class OpenGameStorePageInSteamWebsiteAction : OpenUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getDataProvider().getSteamGameStoreUrl(steamId)
    }
}

class OpenGameWorkshopPageInSteamAction : OpenUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getDataProvider().getSteamGameWorkshopUrlInSteam(steamId)
    }
}

class OpenGameWorkshopPageInSteamWebsiteAction : OpenUrlAction() {
    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
        val steamId = fileInfo.rootInfo.gameType.steamId
        return getDataProvider().getSteamGameWorkshopUrl(steamId)
    }
}

class OpenModPageInSteamAction : OpenUrlAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo is ParadoxModRootInfo
    }

    override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getTargetUrl(fileInfo) != null
    }

    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val steamId = fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()?.descriptorInfo?.remoteFileId ?: return null
        return getDataProvider().getSteamWorkshopUrlInSteam(steamId)
    }
}

class OpenModPageInSteamWebsiteAction : OpenUrlAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo is ParadoxModRootInfo
    }

    override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
        return getTargetUrl(fileInfo) != null
    }

    override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
        val steamId = fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()?.descriptorInfo?.remoteFileId ?: return null
        return getDataProvider().getSteamWorkshopUrl(steamId)
    }

}
