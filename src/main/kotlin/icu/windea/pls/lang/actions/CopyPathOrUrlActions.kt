package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface CopyPathOrUrlActions {
    class Steam : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            return PlsPathService.getInstance().getSteamPath()
        }
    }

    class SteamGame : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
        }
    }

    class GameData : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            val gameType = fileInfo.rootInfo.gameType
            return PlsPathService.getInstance().getGameDataPath(gameType.title)
        }
    }

    class Game : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val fileInfo = getFileInfo(e) ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Game
        }

        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootFile.toNioPath()
        }
    }

    class Mod : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val fileInfo = getFileInfo(e) ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val fileInfo = getFileInfo(e) ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootFile.toNioPath()
        }
    }

    class GameStorePage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val fileInfo = getFileInfo(e) ?: return null
            val gameType = fileInfo.rootInfo.gameType
            val steamId = gameType.steamId
            return PlsUrlService.getInstance().getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val fileInfo = getFileInfo(e) ?: return null
            val gameType = fileInfo.rootInfo.gameType
            val steamId = gameType.steamId
            return PlsUrlService.getInstance().getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun isVisible(e: AnActionEvent): Boolean {
            val fileInfo = getFileInfo(e) ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetUrl(e: AnActionEvent): String? {
            val fileInfo = getFileInfo(e) ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val steamId = fileInfo.rootInfo.steamId?.orNull() ?: return null
            return PlsUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }
}
