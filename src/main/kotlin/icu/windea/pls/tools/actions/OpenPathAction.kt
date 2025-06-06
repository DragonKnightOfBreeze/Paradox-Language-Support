package icu.windea.pls.tools.actions

import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.nio.file.*
import kotlin.io.path.*

abstract class OpenPathAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

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
            return PlsFacade.getDataProvider().getSteamPath()
        }
    }

    class SteamGame : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : OpenPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        }
    }

    class Game : OpenPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Game) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class Mod : OpenPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Mod) return null
            return fileInfo.rootInfo.rootPath
        }
    }
}
