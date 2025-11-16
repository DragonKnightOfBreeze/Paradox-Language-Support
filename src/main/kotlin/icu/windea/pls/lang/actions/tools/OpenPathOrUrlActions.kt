package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.actions.HandleUrlActionBase
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface OpenPathOrUrlActions {
    class Steam : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            return PlsPathService.getSteamPath()
        }
    }

    class SteamGame : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return PlsPathService.getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return PlsPathService.getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val gameType = getGameType(e) ?: return null
            return PlsPathService.getGameDataPath(gameType.title)
        }
    }

    class Game : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Game
        }

        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class Mod : HandlePathActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return fileInfo.rootInfo.rootPath
        }
    }

    class GameStorePageInSteam : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val gameType = getGameType(e) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameStoreUrlInSteam(steamId)
        }
    }

    class GameWorkshopPageInSteam : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val gameType = getGameType(e) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameWorkshopUrlInSteam(steamId)
        }
    }

    class ModPageInSteam : HandleUrlActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val steamId = fileInfo.rootInfo.steamId1?.orNull() ?: return null
            return PlsUrlService.getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class GameStorePage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val gameType = getGameType(e) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val gameType = getGameType(e) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPage : HandleUrlActionBase() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val steamId = fileInfo.rootInfo.steamId1?.orNull() ?: return null
            return PlsUrlService.getSteamWorkshopUrl(steamId)
        }
    }
}

