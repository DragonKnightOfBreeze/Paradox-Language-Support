package icu.windea.pls.core

import java.util.concurrent.*

//直接得到steam的安装路径
//powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Wow6432Node\Valve\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders"
//直接得到steam游戏的安装路径
//powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders"

//游戏安装目录：steamapps/common
//其子目录是游戏名
//创意工坊安装目录：steamapps/common/content
//其子目录是游戏的steamid

//游戏模组安装目录：~\Documents\Paradox Interactive\{gameName}\mod

//使用不会自动清理的缓存
private val steamPathCache = ConcurrentHashMap<String, String>()

fun getSteamPath(): String? {
    val result = steamPathCache.getOrPut("") { doGetSteamPath() }.takeIfNotEmpty()
    return result
}

private fun doGetSteamPath(): String {
    try {
        if(System.getProperty("os.name")?.contains("windows", true) != true) return ""
        val command = "Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Wow6432Node\\Valve\\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders"
        val commandArray = arrayOf("powershell", "-command", command)
        val process = Runtime.getRuntime().exec(commandArray)
        process.waitFor()
        return process.inputStream.reader().use { it.readText() }.trim()
    } catch(e: Exception) {
        return ""
    }
}

fun getSteamGamePath(steamId: String, gameName: String): String? {
    val result = steamPathCache.getOrPut(steamId) { doGetSteamGamePath(steamId) }.takeIfNotEmpty()
    if(result != null) return result
    //不准确，可以放在不同库目录下
    return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\common\$gameName""" }
}

private fun doGetSteamGamePath(steamId: String): String {
    try {
        if(System.getProperty("os.name")?.contains("windows", true) != true) return ""
        val command = "Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders"
        val commandArray = arrayOf("powershell", "-command", command)
        val process = Runtime.getRuntime().exec(commandArray)
        process.waitFor()
        return process.inputStream.reader().use { it.readText() }.trim()
    } catch(e: Exception) {
        return ""
    }
}

fun getSteamWorkshopPath(steamId: String): String? {
    //不准确，可以放在不同库目录下
    return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\workshop\content\$steamId""" }
}

fun getGameDataPath(gameName: String): String? {
    //实际上基于launcher-settings.json中的gameDataPath，有谁会去改这个……
    val userHome = System.getProperty("user.home") ?: return null
    return """$userHome\Documents\Paradox Interactive\$gameName"""
}

/**
 * 得到指定ID对应的Steam游戏商店页面链接。
 */
fun getSteamGameStoreLink(steamId: String): String {
    return "https://store.steampowered.com/app/$steamId/"
}

/**
 * 得到指定ID对应的Steam游戏商店页面链接。（直接在Steam中打开）
 */
fun getSteamGameStoreLinkOnSteam(steamId: String): String {
    return "steam://store/$steamId"
}

/**
 * 得到指定ID对应的Steam游戏创意工坊页面链接。
 */
fun getSteamGameWorkshopLink(steamId: String): String {
    return "https://steamcommunity.com/app/$steamId/workshop/"
}

/**
 * 得到指定ID对应的Steam游戏创意工坊页面链接。（直接在Steam中打开）
 */
fun getSteamGameWorkshopLinkOnSteam(steamId: String): String {
    return "steam://openurl/https://steamcommunity.com/app/$steamId/workshop/"
}

/**
 * 得到指定ID对应的Steam创意工坊链接。
 */
fun getSteamWorkshopLink(steamId: String): String {
    return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}

/**
 * 得到指定ID对应的Steam创意工坊链接。（直接在Steam中打开）
 */
fun getSteamWorkshopLinkOnSteam(steamId: String): String {
    return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}