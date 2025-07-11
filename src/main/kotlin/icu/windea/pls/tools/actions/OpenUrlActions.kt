package icu.windea.pls.tools.actions

import icu.windea.pls.*
import icu.windea.pls.model.*

interface OpenUrlActions {
    class GameStorePageInSteam : OpenUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
            val steamId = fileInfo.rootInfo.gameType.steamId
            return PlsFacade.getDataProvider().getSteamGameStoreUrlInSteam(steamId)
        }
    }

    class GameStorePageInSteamWebsite : OpenUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
            val steamId = fileInfo.rootInfo.gameType.steamId
            return PlsFacade.getDataProvider().getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPageInSteam : OpenUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
            val steamId = fileInfo.rootInfo.gameType.steamId
            return PlsFacade.getDataProvider().getSteamGameWorkshopUrlInSteam(steamId)
        }
    }

    class GameWorkshopPageInSteamWebsite : OpenUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
            val steamId = fileInfo.rootInfo.gameType.steamId
            return PlsFacade.getDataProvider().getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPageInSteam : OpenUrlAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
            return getTargetUrl(fileInfo) != null
        }

        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
            val steamId = fileInfo.rootInfo.steamId ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class ModPageInSteamWebsite : OpenUrlAction() {
        override fun isVisible(fileInfo: ParadoxFileInfo): Boolean {
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun isEnabled(fileInfo: ParadoxFileInfo): Boolean {
            return getTargetUrl(fileInfo) != null
        }

        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
            val steamId = fileInfo.rootInfo.steamId ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopUrl(steamId)
        }
    }
}
