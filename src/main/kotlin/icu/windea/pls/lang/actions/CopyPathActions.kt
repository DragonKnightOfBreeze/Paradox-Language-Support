package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface CopyPathActions {
    class Steam : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            return PlsPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getGameDataPath(gameType)
        }
    }

    class Game : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val contextFile = getContextFile(e) ?: return false
            val fileInfo = contextFile.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Game
        }

        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootFile.toNioPath()
        }
    }

    class Mod : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val contextFile = getContextFile(e) ?: return false
            val fileInfo = contextFile.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootFile.toNioPath()
        }
    }
}
