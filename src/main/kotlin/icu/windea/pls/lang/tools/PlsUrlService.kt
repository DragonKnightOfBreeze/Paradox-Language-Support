package icu.windea.pls.lang.tools

import icu.windea.pls.lang.tools.impl.PlsUrlServiceImpl

interface PlsUrlService {
    /**
     * 得到指定 ID 对应的 Steam 游戏商店页面链接。
     */
    fun getSteamGameStoreUrl(steamId: String): String

    /**
     * 得到指定 ID 对应的 Steam 游戏商店页面链接（直接在 Steam 中打开）。
     */
    fun getSteamGameStoreUrlInSteam(steamId: String): String

    /**
     * 得到指定 ID 对应的 Steam 游戏创意工坊页面链接。
     */
    fun getSteamGameWorkshopUrl(steamId: String): String

    /**
     * 得到指定 ID 对应的 Steam 游戏创意工坊页面链接（直接在 Steam 中打开）。
     */
    fun getSteamGameWorkshopUrlInSteam(steamId: String): String

    /**
     * 得到指定 ID 对应的 Steam 创意工坊链接。
     */
    fun getSteamWorkshopUrl(steamId: String): String

    /**
     * 得到指定 ID 对应的 Steam 创意工坊链接（直接在 Steam 中打开）。
     */
    fun getSteamWorkshopUrlInSteam(steamId: String): String

    /**
     * 在浏览器中打开连接。
     */
    fun openUrl(url: String)

    /**
     * 复制链接到剪贴板。
     */
    fun copyUrl(url: String)

    companion object : PlsUrlService by PlsUrlServiceImpl()
}
