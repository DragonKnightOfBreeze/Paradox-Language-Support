package icu.windea.pls.tools.actions

import icu.windea.pls.*
import icu.windea.pls.model.*

interface CopyUrlActions {
    class GameStorePage : CopyUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String {
            val steamId = fileInfo.rootInfo.gameType.steamId
            return PlsFacade.getDataProvider().getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPage : CopyUrlAction() {
        override fun getTargetUrl(fileInfo: ParadoxFileInfo): String? {
            val steamId = fileInfo.rootInfo.steamId ?: return null
            return PlsFacade.getDataProvider().getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPage : CopyUrlAction() {
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
