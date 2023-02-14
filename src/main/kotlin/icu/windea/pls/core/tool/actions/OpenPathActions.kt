@file:Suppress("ComponentNotRegistered")

package icu.windea.pls.core.tool.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import java.nio.file.*

//com.intellij.ide.actions.RevealFileAction

/**
 * 用于在文件浏览器中打开一个路径。
 */
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
    
    protected open fun isEnabled(fileInfo: ParadoxFileInfo) : Boolean = true
    
    protected abstract fun getTargetPath(fileInfo: ParadoxFileInfo): Path?
}

class OpenSteamPathAction: OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        return getSteamPath()?.toPathOrNull()
    }
}

class OpenSteamGamePathAction: OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getSteamGamePath(gameType.gameSteamId, gameType.gameName)?.toPathOrNull()
    }
}

class OpenSteamWorkshopPathAction: OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getSteamWorkshopPath(gameType.gameSteamId)?.toPathOrNull()
    }
}

class OpenGameDataPathAction: OpenPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getGameDataPath(gameType.gameName)?.toPathOrNull()
    }
}