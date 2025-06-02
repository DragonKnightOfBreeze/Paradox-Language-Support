package icu.windea.pls.tools.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.nio.file.*

abstract class CopyPathAction : DumbAwareAction() {
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
        CopyPasteManager.getInstance().setContents(StringSelection(targetPath.toString()))
    }

    protected open fun isVisible(fileInfo: ParadoxFileInfo): Boolean = true

    protected open fun isEnabled(fileInfo: ParadoxFileInfo): Boolean = true

    protected abstract fun getTargetPath(fileInfo: ParadoxFileInfo): Path?

    class Steam : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            return PlsFacade.getDataProvider().getSteamPath()?.toPathOrNull()
        }
    }

    class SteamGame : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)?.toPathOrNull()
        }
    }

    class SteamWorkshop : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)?.toPathOrNull()
        }
    }

    class GameData : CopyPathAction() {
        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            val gameType = fileInfo.rootInfo.gameType
            return PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull()
        }
    }

    class Game : CopyPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Game) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class Mod : CopyPathAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(fileInfo: ParadoxFileInfo): Path? {
            if (fileInfo.rootInfo !is ParadoxRootInfo.Mod) return null
            return fileInfo.rootInfo.rootPath
        }
    }
}
