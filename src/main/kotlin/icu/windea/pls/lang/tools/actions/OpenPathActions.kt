@file:Suppress("ComponentNotRegistered")

package icu.windea.pls.lang.tools.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import java.nio.file.*
import kotlin.io.path.*

abstract class OpenPathAction : DumbAwareAction() {
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
            val targetPath = getTargetPath(fileInfo)
            if(targetPath != null) {
                presentation.description = templatePresentation.description + " (" + targetPath + ")"
            }
        }
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileInfo = virtualFile.fileInfo ?: return
        val targetPath = getTargetPath(fileInfo) ?: return //ignore
        when {
            targetPath.isDirectory() -> RevealFileAction.openDirectory(targetPath)
            else -> RevealFileAction.openFile(targetPath)
        }
    }
    
    protected open fun isVisible(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected open fun isEnabled(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected abstract fun getTargetPath(fileInfo: ParadoxFileInfo): Path?
}

class OpenSteamPathAction : OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        return PathProvider.getSteamPath()?.toPathOrNull()
    }
}

class OpenSteamGamePathAction : OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return PathProvider.getSteamGamePath(gameType.steamId, gameType.title)?.toPathOrNull()
    }
}

class OpenSteamWorkshopPathAction : OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return PathProvider.getSteamWorkshopPath(gameType.steamId)?.toPathOrNull()
    }
}

class OpenGameDataPathAction : OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return PathProvider.getGameDataPath(gameType.title)?.toPathOrNull()
    }
}

class OpenGamePathAction : OpenPathAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo is ParadoxModRootInfo
    }
    
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        if(fileInfo.rootInfo !is ParadoxGameRootInfo) return null
        return fileInfo.rootInfo.gameRootPath
    }
}

class OpenModPathAction : OpenPathAction() {
    override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
        return fileInfo.rootInfo is ParadoxModRootInfo
    }
    
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        if(fileInfo.rootInfo !is ParadoxModRootInfo) return null
        return fileInfo.rootInfo.gameRootPath
    }
}
