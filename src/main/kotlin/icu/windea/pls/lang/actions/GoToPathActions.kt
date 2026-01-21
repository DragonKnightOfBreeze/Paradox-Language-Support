package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileSystemTree
import icu.windea.pls.lang.tools.PlsPathService
import java.nio.file.Path

interface GoToPathActions {
    class Steam : GoToPathActionBase() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            return  PlsPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : GoToPathActionBase() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val rootInfo = fileInfo.rootInfo
            val gameType = rootInfo.gameType
            return PlsPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : GoToPathActionBase() {
        override fun shouldExpand(fileChooser: FileSystemTree, e: AnActionEvent): Boolean {
            return true
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val rootInfo = fileInfo.rootInfo
            val gameType = rootInfo.gameType
            return PlsPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : GoToPathActionBase() {
        override fun shouldExpand(fileChooser: FileSystemTree, e: AnActionEvent): Boolean {
            return true
        }

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val rootInfo = fileInfo.rootInfo
            val gameType = rootInfo.gameType
            return PlsPathService.getInstance().getGameDataPath(gameType.title)
        }
    }
}
