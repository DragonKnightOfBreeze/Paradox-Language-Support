package icu.windea.pls.lang.tools

import com.intellij.openapi.components.serviceOrNull

interface PlsUrlService {
    /**
     * 得到 [steamId] 对应的 Steam 游戏的商店页面链接。
     */
    fun getSteamGameStoreUrl(steamId: String): String

    /**
     * 得到 [steamId] 对应的 Steam 游戏的创意工坊页面链接。
     */
    fun getSteamGameWorkshopUrl(steamId: String): String

    /**
     * 得到 [steamId] 对应的 Steam 创意工坊物品的页面链接。
     */
    fun getSteamWorkshopUrl(steamId: String): String

    /**
     * 得到 [steamId] 对应的 Steam 游戏的商店页面链接（直接在 Steam 中打开）。
     *
     * 参见：[Steam browser protocol - Valve Developer Community](https://developer.valvesoftware.com/wiki/Steam_browser_protocol)
     */
    fun getSteamGameStoreUrlInSteam(steamId: String): String

    /**
     * 得到 [steamId] 对应的 Steam 游戏的创意工坊页面链接（直接在 Steam 中打开）。
     *
     * 参见：[Steam browser protocol - Valve Developer Community](https://developer.valvesoftware.com/wiki/Steam_browser_protocol)
     */
    fun getSteamGameWorkshopUrlInSteam(steamId: String): String

    /**
     * 得到 [steamId] 对应的 Steam 创意工坊物品的页面链接（直接在 Steam 中打开）。
     *
     * 参见：[Steam browser protocol - Valve Developer Community](https://developer.valvesoftware.com/wiki/Steam_browser_protocol)
     */
    fun getSteamWorkshopUrlInSteam(steamId: String): String

    /**
     * 得到用于启动 [steamId] 对应的 Steam 游戏的链接（通过 Steam 启动）。
     *
     * 参见：[Steam browser protocol - Valve Developer Community](https://developer.valvesoftware.com/wiki/Steam_browser_protocol)
     */
    fun getSteamGameLaunchUrl(steamId: String): String

    /**
     * 是否是使用 Steam 超链接协议的链接。
     *
     * 参见：[Steam browser protocol - Valve Developer Community](https://developer.valvesoftware.com/wiki/Steam_browser_protocol)
     */
    fun isSteamUrl(url: String): Boolean

    /**
     * 在浏览器中打开链接。
     */
    fun openUrl(url: String)

    /**
     * 复制链接到剪贴板。
     */
    fun copyUrl(url: String)

    /**
     * 是否是使用自定义协议的链接（如 Steam 超链接协议）的链接。
     */
    fun isCustomUrl(url: String): Boolean

    /**
     * 打开使用自定义协议的链接（如 Steam 超链接协议）。
     */
    fun openCustomUrl(url: String)

    companion object {
        @JvmStatic
        fun getInstance(): PlsUrlService = serviceOrNull() ?: PlsUrlServiceImpl()
    }
}
