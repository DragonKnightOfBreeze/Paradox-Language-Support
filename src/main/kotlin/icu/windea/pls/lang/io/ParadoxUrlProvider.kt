package icu.windea.pls.lang.io

import com.intellij.openapi.components.*

@Service
class ParadoxUrlProvider {
    /**
     * 得到指定ID对应的Steam游戏商店页面链接。
     */
    fun getSteamGameStoreUrl(steamId: String): String {
        return "https://store.steampowered.com/app/$steamId/"
    }
    
    /**
     * 得到指定ID对应的Steam游戏商店页面链接。（直接在Steam中打开）
     */
    fun getSteamGameStoreUrlInSteam(steamId: String): String {
        return "steam://store/$steamId"
    }
    
    /**
     * 得到指定ID对应的Steam游戏创意工坊页面链接。
     */
    fun getSteamGameWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/app/$steamId/workshop/"
    }
    
    /**
     * 得到指定ID对应的Steam游戏创意工坊页面链接。（直接在Steam中打开）
     */
    fun getSteamGameWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/app/$steamId/workshop/"
    }
    
    /**
     * 得到指定ID对应的Steam创意工坊链接。
     */
    fun getSteamWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }
    
    /**
     * 得到指定ID对应的Steam创意工坊链接。（直接在Steam中打开）
     */
    fun getSteamWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }
}
