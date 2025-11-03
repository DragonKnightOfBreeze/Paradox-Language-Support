package icu.windea.pls.lang.tools.impl

import com.intellij.openapi.components.Service
import icu.windea.pls.lang.tools.PlsUrlService

@Service
class PlsUrlServiceImpl : PlsUrlService {
    override fun getSteamGameStoreUrl(steamId: String): String {
        return "https://store.steampowered.com/app/$steamId/"
    }

    override fun getSteamGameStoreUrlInSteam(steamId: String): String {
        return "steam://store/$steamId"
    }

    override fun getSteamGameWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamGameWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }

    override fun getSteamWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }
}
