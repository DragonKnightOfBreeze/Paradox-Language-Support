package icu.windea.pls.lang

import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.console.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import java.nio.file.*
import java.util.concurrent.*
import kotlin.io.path.*

/**
 * 用于提供一些需要动态获取的数据。
 */
@Service
class PlsDataProvider {
    private val steamPathCache = ConcurrentHashMap<String, Path>()
    private val EMPTY_PATH = Path.of("")

    fun init() {
        //preload cached values
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            launch {
                getSteamPath()
            }
            ParadoxGameType.entries.forEach { gameType ->
                launch {
                    getSteamGamePath(gameType.steamId, gameType.title)
                }
            }
        }
    }

    //region Paths

    //Steam的实际安装路径：（通过特定命令获取）
    //Steam游戏的实际安装路径：（通过特定命令获取）
    //Steam游戏的默认安装路径：steamapps/common（其子目录是游戏名）
    //创意工坊安装目录：steamapps/common/content（其子目录是游戏的steamId）
    //游戏模组安装目录：~\Documents\Paradox Interactive\${gameName}\mod

    /**
     * 得到Steam目录的路径。
     */
    fun getSteamPath(): Path? {
        return steamPathCache.getOrPut("") { doGetSteamPath() ?: EMPTY_PATH }.takeIf { it !== EMPTY_PATH }
    }

    private fun doGetSteamPath(): Path? {
        return when (OS.value) {
            OS.Windows -> {
                //查找注册表
                val command = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/WOW6432Node/Valve/Steam').InstallPath"
                val commandResult = runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrNull()
                commandResult?.toPathOrNull()?.formatted()
            }
            OS.Linux -> {
                //默认路经（不准确，但是已经足够）
                Path("~", ".local", "share", "Steam").formatted()
            }
        }
    }

    /**
     * 得到指定ID对应的Steam游戏目录的路径。
     */
    fun getSteamGamePath(steamId: String, gameName: String): Path? {
        return steamPathCache.getOrPut(steamId) { doGetSteamGamePath(steamId, gameName) ?: EMPTY_PATH }.takeIf { it !== EMPTY_PATH }
    }

    private fun doGetSteamGamePath(steamId: String, gameName: String): Path? {
        return when (OS.value) {
            OS.Windows -> {
                //查找注册表
                val command = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/Microsoft/Windows/CurrentVersion/Uninstall/Steam App ${steamId}').InstallLocation"
                val commandResult = runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrNull()
                val fromCommandResult = commandResult?.toPathOrNull()?.formatted()
                if (fromCommandResult != null) return fromCommandResult

                //默认路经（不准确，可以放在不同库目录下）
                val steamPath = getSteamPath()?.toString() ?: return null
                Path(steamPath, "steamapps", "common", gameName).formatted()
            }
            OS.Linux -> {
                //默认路经（不准确，可以放在不同库目录下）
                val steamPath = getSteamPath()?.toString() ?: return null
                Path(steamPath, "steamapps", "common", gameName).formatted()
            }
        }
    }

    /**
     * 得到指定ID对应的Steam创意工坊目录的路径。
     */
    fun getSteamWorkshopPath(steamId: String): Path? {
        return doGetSteamWorkshopPath(steamId)
    }

    private fun doGetSteamWorkshopPath(steamId: String): Path? {
        //不准确，可以放在不同库目录下
        val steamPath = getSteamPath()?.toString() ?: return null
        return Path(steamPath, "steamapps", "workshop", "content", steamId).formatted()
    }

    /**
     * 得到指定游戏名对应的游戏数据目录的路径。
     */
    fun getGameDataPath(gameName: String): Path? {
        return doGetGameDataPath(gameName)
    }

    private fun doGetGameDataPath(gameName: String): Path? {
        //实际上应当基于launcher-settings.json中的gameDataPath
        return when (OS.value) {
            OS.Windows -> Path("~", "Documents", "Paradox Interactive", gameName).formatted()
            OS.Linux -> Path("~", ".local", "share", "Paradox Interactive", gameName).formatted()
        }
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

