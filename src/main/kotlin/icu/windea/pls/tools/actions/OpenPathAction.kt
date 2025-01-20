package icu.windea.pls.tools.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
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
        if (presentation.isVisible) {
            val targetPath = getTargetPath(fileInfo)
            if (targetPath != null) {
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

    class Steam : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            return getDataProvider().getSteamPath()?.toPathOrNull()
        }
    }

    class SteamGame : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)?.toPathOrNull()
        }
    }

    class SteamWorkshop : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return getDataProvider().getSteamWorkshopPath(gameType.steamId)?.toPathOrNull()
        }
    }

    class GameData : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull()
        }
    }

    class Game : OpenPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxModRootInfo
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxGameRootInfo) return null
            return fileInfo.rootInfo.gameRootPath
        }
    }

    class Mod : OpenPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxModRootInfo
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxModRootInfo) return null
            return fileInfo.rootInfo.gameRootPath
        }
    }
}
