package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

interface CopyPathActions {
    abstract class Base : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath(e) ?: return
            CopyPasteManager.getInstance().setContents(StringSelection(targetPath.toString()))
        }
    }

    class Steam : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            return PlsFacade.getDataProvider().getSteamPath()
        }
    }

    class SteamGame : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        }
    }

    class Game : Base() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Game
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class Mod : Base() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootPath
        }
    }
}
