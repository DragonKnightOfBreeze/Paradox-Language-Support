package icu.windea.pls.lang.util.io

import com.intellij.openapi.components.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
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

@Service
class ParadoxPathProvider(private val coroutineScope: CoroutineScope) {
    //使用不会自动清理的缓存
    private val steamPathCache = ConcurrentHashMap<String, String>()
    
    fun init() {
        //preload cached values
        coroutineScope.launch {
            launch {
                steamPathCache.put("", doGetSteamPath())
            }
            ParadoxGameType.entries.forEach { gameType ->
                launch {
                    steamPathCache.put(gameType.steamId, doGetSteamGamePath(gameType.steamId))
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
    
    /**
     * 得到指定ID对应的Steam游戏目录的路径。
     */
    fun getSteamGamePath(steamId: String, gameName: String): String? {
        val result = steamPathCache.computeIfAbsent(steamId) { doGetSteamGamePath(steamId) }.orNull()
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
    
    /**
     * 得到指定ID对应的Steam创意工坊目录的路径。
     */
    fun getSteamWorkshopPath(steamId: String): String? {
        //不准确，可以放在不同库目录下
        return getSteamPath()?.let { steamPath -> """$steamPath\steamapps\workshop\content\$steamId""" }
    }
    
    /**
     * 得到指定游戏名对应的游戏数据目录的路径。
     */
    fun getGameDataPath(gameName: String): String? {
        //实际上基于launcher-settings.json中的gameDataPath，有谁会去改这个……
        val userHome = System.getProperty("user.home") ?: return null
        return """$userHome\Documents\Paradox Interactive\$gameName"""
    }
}

