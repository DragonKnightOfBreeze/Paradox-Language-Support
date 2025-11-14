package icu.windea.pls.lang.tools.impl

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.ide.CopyPasteManager
import icu.windea.pls.core.console.CommandType
import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.formatted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.util.OS
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.constants.PlsPathConstants
import java.awt.datatransfer.StringSelection
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

class PlsPathServiceImpl : PlsPathService {
    private val steamPathCache = ConcurrentHashMap<String, Path>()
    private val emptyPath = Path.of("")

    // Steam的实际安装路径：（通过特定命令获取）
    // Steam游戏的实际安装路径：（通过特定命令获取）
    // Steam游戏的默认安装路径：steamapps/common（其子目录是游戏名）
    // 创意工坊安装目录：steamapps/common/content（其子目录是游戏的steamId）
    // 游戏模组安装目录：~\Documents\Paradox Interactive\${gameName}\mod

    override fun getSteamPath(): Path? {
        return steamPathCache.getOrPut("") { doGetSteamPath() ?: emptyPath }.takeIf { it !== emptyPath }
    }

    private fun doGetSteamPath(): Path? {
        return when (OS.value) {
            OS.Windows -> {
                // 查找注册表
                val command = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/WOW6432Node/Valve/Steam').InstallPath"
                val commandResult = runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrNull()
                val steamPath = commandResult?.orNull()?.toPathOrNull()?.formatted()
                steamPath
            }
            OS.Linux -> {
                // 默认路径（不准确，但是已经足够）
                val steamPath = PlsPathConstants.userHome.resolve(Path.of(".local", "share", "Steam")).formatted()
                steamPath
            }
        }
    }

    override fun getSteamGamePath(steamId: String, gameName: String): Path? {
        return steamPathCache.getOrPut(steamId) { doGetSteamGamePath(steamId, gameName) ?: emptyPath }.takeIf { it !== emptyPath }
    }

    private fun doGetSteamGamePath(steamId: String, gameName: String): Path? {
        return when (OS.value) {
            OS.Windows -> {
                // 查找注册表
                val command = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/Microsoft/Windows/CurrentVersion/Uninstall/Steam App ${steamId}').InstallLocation"
                val commandResult = runCatchingCancelable { executeCommand(command, CommandType.POWER_SHELL) }.getOrNull()
                val fromCommandResult = commandResult?.orNull()?.toPathOrNull()?.formatted()
                if (fromCommandResult != null) return fromCommandResult

                // 默认路径（不准确，可以放在不同库目录下）
                val steamPath = getSteamPath() ?: return null
                val steamGamePath = steamPath.resolve(Path("steamapps", "common", gameName)).formatted()
                steamGamePath
            }
            OS.Linux -> {
                // 默认路径（不准确，可以放在不同库目录下）
                val steamPath = getSteamPath() ?: return null
                val steamGamePath = steamPath.resolve(Path("steamapps", "common", gameName)).formatted()
                steamGamePath
            }
        }
    }

    override fun getSteamWorkshopPath(steamId: String): Path? {
        return doGetSteamWorkshopPath(steamId)
    }

    private fun doGetSteamWorkshopPath(steamId: String): Path? {
        // 不准确，可以放在不同库目录下
        val steamPath = getSteamPath() ?: return null
        return steamPath.resolve(Path("steamapps", "workshop", "content", steamId)).formatted()
    }

    override fun getGameDataPath(gameName: String): Path? {
        return doGetGameDataPath(gameName)
    }

    private fun doGetGameDataPath(gameName: String): Path? {
        // 实际上应当基于launcher-settings.json中的gameDataPath
        return when (OS.value) {
            OS.Windows -> PlsPathConstants.userHome.resolve(Path("Documents", "Paradox Interactive", gameName)).formatted()
            OS.Linux -> PlsPathConstants.userHome.resolve(Path(".local", "share", "Paradox Interactive", gameName)).formatted()
        }
    }

    override fun openPath(path: Path) {
        when {
            path.isDirectory() -> RevealFileAction.openDirectory(path)
            else -> RevealFileAction.openFile(path)
        }
    }

    override fun copyPath(path: Path) {
        CopyPasteManager.getInstance().setContents(StringSelection(path.toString()))
    }
}
