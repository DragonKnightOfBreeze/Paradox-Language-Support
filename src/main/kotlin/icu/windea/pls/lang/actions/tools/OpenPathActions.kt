package icu.windea.pls.lang.actions.tools

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path
import kotlin.io.path.isDirectory

interface OpenPathActions {
    abstract class Base : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath(e) ?: return // ignore
            when {
                targetPath.isDirectory() -> RevealFileAction.openDirectory(targetPath)
                else -> RevealFileAction.openFile(targetPath)
            }
        }
    }

    class Steam : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            return PlsPathService.getSteamPath()
        }
    }

    class SteamGame : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsPathService.getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsPathService.getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : Base() {
        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsPathService.getGameDataPath(gameType.title)
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
