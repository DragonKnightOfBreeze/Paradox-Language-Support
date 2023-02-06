package icu.windea.pls.core

//直接得到steam的安装路径
//powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Wow6432Node\Valve\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders"
//直接得到steam游戏的安装路径
//powershell -command "Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders"

//游戏安装目录：steamapps/common
//其子目录是游戏名
//创意工坊安装目录：steamapps/common/content
//其子目录是游戏的steamid

//游戏模组安装目录：~\Documents\Paradox Interactive\{gameName}\mod

private val getSteamPathCommand =
    arrayOf("powershell", "-command", "Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Wow6432Node\\Valve\\Steam' | Select-Object InstallPath | Format-Table -HideTableHeaders")

fun getSteamPath(): String? {
    try {
        if(System.getProperty("os.name")?.contains("windows", true) != true) return null
        val command = getSteamPathCommand
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        val result = process.inputStream.reader().use { it.readText() }.trim().takeIfNotEmpty()
        return result
    } catch(e: Exception) {
        return null
    }
}

private fun getSteamGamePathCommand(steamId: String) =
    arrayOf("powershell", "-command", "Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Steam App ${steamId}' | Select-Object InstallLocation | Format-Table -HideTableHeaders")

fun getSteamGamePath(gameSteamId: String, gameName: String): String? {
    try {
        if(System.getProperty("os.name")?.contains("windows", true) != true) return null
        val command = getSteamGamePathCommand(gameSteamId)
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        val result = process.inputStream.reader().use { it.readText() }.trim().takeIfNotEmpty()
        if(result != null) return result
        //不准确，可以放在不同库目录下
        val resultFromSteamPath = getSteamPath()?.let { steamPath -> """$steamPath\steamapps\common\$gameName""" }
        return resultFromSteamPath
    } catch(e: Exception) {
        return null
    }
}

fun getSteamWorkshopPath(gameSteamId: String): String? {
    //不准确，可以放在不同库目录下
    return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\workshop\content\$gameSteamId""" }
}

fun getGameDataPath(gameName: String): String? {
    //实际上基于launcher-settings.json中的gameDataPath，有谁会去改这个……
    val userHome = System.getProperty("user.home") ?: return null
    return """$userHome\Documents\Paradox Interactive\$gameName"""
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