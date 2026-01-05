package icu.windea.pls.lang.tools.impl

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.ide.CopyPasteManager
import icu.windea.pls.lang.tools.PlsUrlService
import java.awt.datatransfer.StringSelection

class PlsUrlServiceImpl : PlsUrlService {
    override fun getSteamGameStoreUrl(steamId: String): String {
        return "https://store.steampowered.com/app/$steamId/"
    }

    override fun getSteamGameWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }

    override fun getSteamGameStoreUrlInSteam(steamId: String): String {
        return "steam://store/$steamId"
    }

    override fun getSteamGameWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }

    override fun getSteamGameLaunchUrl(steamId: String): String {
        return "steam://launch/$steamId"
    }

    override fun openUrl(url: String) {
        BrowserUtil.open(url)
    }

    override fun copyUrl(url: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(url))
    }
}
