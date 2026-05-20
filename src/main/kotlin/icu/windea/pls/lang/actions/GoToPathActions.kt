package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileSystemTree
import icu.windea.pls.lang.tools.SpecialPathService
import java.nio.file.Path

interface GoToPathActions {
    class Steam : GoToPathActionBase() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            return SpecialPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : GoToPathActionBase() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : GoToPathActionBase() {
        override fun shouldExpand(fileChooser: FileSystemTree, e: AnActionEvent): Boolean {
            return true
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return SpecialPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : GoToPathActionBase() {
        override fun shouldExpand(fileChooser: FileSystemTree, e: AnActionEvent): Boolean {
            return true
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return SpecialPathService.getInstance().getGameDataPath(gameType)
        }
    }
}
