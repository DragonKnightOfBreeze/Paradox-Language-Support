@file:Suppress("ComponentNotRegistered")

package icu.windea.pls.core.tool.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.nio.file.*

/**
 * 用于复制一个路径到系统剪贴板。
 */
abstract class CopyPathAction : DumbAwareAction() {
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
        CopyPasteManager.getInstance().setContents(StringSelection(targetPath.toString()))
    }
    
    protected open fun isVisible(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected open fun isEnabled(fileInfo: ParadoxFileInfo): Boolean = true
    
    protected abstract fun getTargetPath(fileInfo: ParadoxFileInfo): Path?
}

class CopySteamPathAction : CopyPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        return getSteamPath()?.toPathOrNull()
    }
}

class CopySteamGamePathAction : CopyPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getSteamGamePath(gameType.steamId, gameType.title)?.toPathOrNull()
    }
}

class CopySteamWorkshopPathAction : CopyPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getSteamWorkshopPath(gameType.steamId)?.toPathOrNull()
    }
}

class CopyGameDataPathAction : CopyPathAction() {
    override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
        val gameType = fileInfo.rootInfo.gameType
        return getGameDataPath(gameType.title)?.toPathOrNull()
    }
}