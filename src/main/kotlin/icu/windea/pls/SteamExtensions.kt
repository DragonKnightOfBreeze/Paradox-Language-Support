package icu.windea.pls

//# 直接得到steam的安装路径
//powershell -command "Get-ItemProperty -Path HKLM:\SOFTWARE\Wow6432Node\Valve\Steam | Select-Object InstallPath | Format-Table -HideTableHeaders"

//游戏安装目录：steamapps/common
//其子目录是游戏名
//创意工坊安装目录：steamapps/c/content
//其子目录是游戏的steamid

//游戏模组安装目录：~\Documents\Paradox Interactive\{gameName}\mod

private val getSteamPathCommand = arrayOf("powershell", "-command", "Get-ItemProperty -Path HKLM:\\SOFTWARE\\Wow6432Node\\Valve\\Steam | Select-Object InstallPath | Format-Table -HideTableHeaders")

fun getSteamPath(): String? {
	try {
		if(System.getProperty("os.name")?.contains("windows", true) != true) return null
		val process = Runtime.getRuntime().exec(getSteamPathCommand)
		process.waitFor()
		return process.inputStream.reader().use { it.readText() }.trim()
	} catch(e: Exception) {
		return null
	}
}

fun getSteamGamePath(gameName: String): String? {
	return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\common\$gameName""" }
}

fun getSteamWorkshopPath(gameId: String): String? {
	return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\workshop\content\$gameId""" }
}

fun getGameModPath(gameName: String): String? {
	val userHome = System.getProperty("user.home") ?: return null
	return """$userHome\Documents\Paradox Interactive\$gameName\mod"""
}

fun getSteamWorkshopLink(steamId: String): String {
	return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}

fun getSteamWorkshopLinkOnSteam(steamId: String): String {
	return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}