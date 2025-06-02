package icu.windea.pls.lang

import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import java.util.concurrent.*

/**
 * 用于提供一些需要动态获取的数据。
 */
@Service
class PlsDataProvider {
    fun init() {
        //preload cached values
        initForPaths()
    }

    //region Paths

    //直接得到steam的安装路径
    //powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Wow6432Node\Valve\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders"
    //直接得到steam游戏的安装路径
    //powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders"

    //游戏安装目录：steamapps/common
    //其子目录是游戏名
    //创意工坊安装目录：steamapps/common/content
    //其子目录是游戏的steamid

    //游戏模组安装目录：~\Documents\Paradox Interactive\${gameName}\mod

    //使用不会自动清理的缓存
    private val steamPathCache = ConcurrentHashMap<String, String>()

    private fun initForPaths() {
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            launch {
                getSteamPath()
            }
            ParadoxGameType.entries.forEach { gameType ->
                launch {
                    getSteamGamePath(gameType.steamId)
                }
            }
        }
    }

    /**
     * 得到Steam目录的路径。
     */
    fun getSteamPath(): String? {
        val result = steamPathCache.computeIfAbsent("") { doGetSteamPath() }.orNull()
        return result
    }

    private fun doGetSteamPath(): String {
        val command = """Get-ItemProperty -Path 'HKLM:\SOFTWARE\Wow6432Node\Valve\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders"""
        return runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrDefault("")
    }

    /**
     * 得到指定ID对应的Steam游戏目录的路径。
     */
    fun getSteamGamePath(steamId: String, gameName: String? = null): String? {
        val result = steamPathCache.computeIfAbsent(steamId) { doGetSteamGamePath(steamId) }.orNull()
        if (result != null) return result
        if (gameName != null) return doGetFallbackSteamGamePath(gameName)
        return null
    }

    private fun doGetSteamGamePath(steamId: String): String {
        val command = """Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders"""
        return runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrDefault("")
    }

    private fun doGetFallbackSteamGamePath(gameName: String): String? {
        //不准确，可以放在不同库目录下
        val steamPath = getSteamPath() ?: return null
        return """$steamPath\steamapps\common\$gameName"""
    }

    /**
     * 得到指定ID对应的Steam创意工坊目录的路径。
     */
    fun getSteamWorkshopPath(steamId: String): String? {
        //不准确，可以放在不同库目录下
        val steamPath = getSteamPath() ?: return null
        return """$steamPath\steamapps\workshop\content\$steamId"""
    }

    /**
     * 得到指定游戏名对应的游戏数据目录的路径。
     */
    fun getGameDataPath(gameName: String): String? {
        //实际上应当基于launcher-settings.json中的gameDataPath
        val userHome = System.getProperty("user.home") ?: return null
        return """$userHome\Documents\Paradox Interactive\$gameName"""
    }

    //endregion

    //region Urls

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

    //endregion
}

