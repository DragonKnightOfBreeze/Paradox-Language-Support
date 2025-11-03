package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.actions.HandleUrlActionBase
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.steamId
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
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsPathService.getSteamGamePath(gameType.steamId, gameType.title)
        }
    }

    class SteamWorkshop : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            return PlsPathService.getSteamWorkshopPath(gameType.steamId)
        }
    }

    class GameData : HandlePathActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
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
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameStoreUrlInSteam(steamId)
        }
    }

    class GameStorePageInSteamWebsite : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPageInSteam : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameWorkshopUrlInSteam(steamId)
        }
    }

    class GameWorkshopPageInSteamWebsite : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameWorkshopUrl(steamId)
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
            val steamId = fileInfo.rootInfo.steamId?.orNull() ?: return null
            return PlsUrlService.getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class ModPageInSteamWebsite : HandleUrlActionBase() {
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
            val steamId = fileInfo.rootInfo.steamId?.orNull() ?: return null
            return PlsUrlService.getSteamWorkshopUrl(steamId)
        }
    }
}
