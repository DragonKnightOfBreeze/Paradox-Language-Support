package icu.windea.pls.lang.tools

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.util.system.OS
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.execution.CommandType
import icu.windea.pls.core.executeCommandLine
import icu.windea.pls.core.formatted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsPaths
import kotlinx.coroutines.launch
import java.awt.datatransfer.StringSelection
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

class PlsPathServiceImpl : PlsPathService, Disposable {
    private val steamPathCache = ConcurrentHashMap<String, Path>()
    private val emptyPath = Path.of("")

    // Steam 的实际安装路径：（通过特定命令获取）
    // Steam 游戏的实际安装路径：（通过特定命令获取）
    // Steam 游戏的默认安装路径：`steamapps/common`（其子目录是游戏名）
    // 创意工坊安装目录：`steamapps/common/content`（其子目录是游戏的 steamId）
    // 游戏模组安装目录：`~\Documents\Paradox Interactive\{gameName}\mod`

    override fun initAsync() {
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch { getSteamPath() }
        ParadoxGameType.getAll().forEach { gameType ->
            coroutineScope.launch { getSteamGamePath(gameType.steamId, gameType.title) }
        }
    }

    override fun getSteamPath(): Path? {
        return steamPathCache.computeIfAbsent("") { resolveSteamPath() ?: emptyPath }.takeIf { it !== emptyPath }
    }

    private fun resolveSteamPath(): Path? {
        return when (OS.CURRENT) {
            OS.Windows -> resolveSteamPathFromRegistry()
            else -> resolveSteamPathForLinux()
        }
    }

    private fun resolveSteamPathFromRegistry(): Path? {
        // 64位系统：查找 WOW6432Node 重定向键
        val command64 = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/WOW6432Node/Valve/Steam' -ErrorAction SilentlyContinue).InstallPath"
        val result64 = runCatchingCancelable { executeCommandLine(command64, CommandType.POWER_SHELL) }.getOrNull()
        result64?.orNull()?.toPathOrNull()?.formatted()?.let { return it }

        // 32位系统回退：查找非重定向键
        val command32 = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/Valve/Steam' -ErrorAction SilentlyContinue).InstallPath"
        val result32 = runCatchingCancelable { executeCommandLine(command32, CommandType.POWER_SHELL) }.getOrNull()
        result32?.orNull()?.toPathOrNull()?.formatted()?.let { return it }

        return null
    }

    private fun resolveSteamPathForLinux(): Path {
        // 按优先级依次尝试已知的 Steam 安装路径
        val home = PlsPaths.userHome
        val candidates = listOf(
            home.resolve(Path(".local", "share", "Steam")),
            home.resolve(Path(".steam", "debian-installation")),
            home.resolve(Path(".steam", "steam")),
            home.resolve(Path("snap", "steam", "common", ".local", "share", "Steam")),
            home.resolve(Path(".var", "app", "com.valvesoftware.Steam", ".local", "share", "Steam")),
        )
        val result = candidates.firstOrNull { it.isDirectory() } ?: candidates.first()
        return result.formatted()
    }

    override fun getSteamGamePath(steamId: String, gameName: String): Path? {
        if (steamId.isEmpty() || gameName.isEmpty()) return null
        return steamPathCache.getOrPut(steamId) { resolveSteamGamePath(steamId, gameName) ?: emptyPath }.takeIf { it !== emptyPath }
    }

    private fun resolveSteamGamePath(steamId: String, gameName: String): Path? {
        resolveSteamGamePathFromVdf(gameName)?.let { return it }
        if (OS.CURRENT == OS.Windows) resolveSteamGamePathFromRegistry(steamId)?.let { return it }
        return null
    }

    private fun resolveSteamGamePathFromVdf(gameName: String): Path? {
        // 通过 libraryfolders.vdf 扫描各库目录（Windows 和 Linux 通用，更可靠）
        val steamPath = getSteamPath() ?: return null
        for (library in resolveLibraryFolders(steamPath)) {
            val gamePath = library.resolve("steamapps").resolve("common").resolve(gameName)
            if (gamePath.isDirectory()) return gamePath.formatted()
        }
        return null
    }

    private fun resolveSteamGamePathFromRegistry(steamId: String): Path? {
        // 64位系统：查找 WOW6432Node 重定向键
        val command64 = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/WOW6432Node/Microsoft/Windows/CurrentVersion/Uninstall/Steam App ${steamId}' -ErrorAction SilentlyContinue).InstallLocation"
        val result64 = runCatchingCancelable { executeCommandLine(command64, CommandType.POWER_SHELL) }.getOrNull()
        result64?.orNull()?.toPathOrNull()?.formatted()?.let { return it }

        // 32位系统回退：查找非重定向键
        val command32 = "(Get-ItemProperty -Path 'HKLM:/SOFTWARE/Microsoft/Windows/CurrentVersion/Uninstall/Steam App ${steamId}' -ErrorAction SilentlyContinue).InstallLocation"
        val result32 = runCatchingCancelable { executeCommandLine(command32, CommandType.POWER_SHELL) }.getOrNull()
        result32?.orNull()?.toPathOrNull()?.formatted()?.let { return it }

        return null
    }

    override fun getSteamGameWorkshopPath(steamId: String): Path? {
        if (steamId.isEmpty()) return null
        return resolveSteamGameWorkshopPath(steamId)
    }

    private fun resolveSteamGameWorkshopPath(steamId: String): Path? {
        return resolveSteamWorkshopPathFromVdf(steamId)
    }

    private fun resolveSteamWorkshopPathFromVdf(steamId: String): Path? {
        val steamPath = getSteamPath() ?: return null
        // 扫描各库目录，Workshop 内容与游戏本体位于同一库目录下
        for (library in resolveLibraryFolders(steamPath)) {
            val workshopPath = library.resolve("steamapps").resolve("workshop").resolve("content").resolve(steamId)
            if (workshopPath.isDirectory()) return workshopPath.formatted()
        }
        return null
    }

    /**
     * 解析 `steamapps/libraryfolders.vdf`，返回所有已知 Steam 库目录（含主目录自身）。
     * 此文件在所有平台的 Steam 安装中均存在，是定位游戏安装目录最可靠的方式。
     */
    private fun resolveLibraryFolders(steamPath: Path): List<Path> {
        val libraries = mutableListOf(steamPath)
        val vdfPath = steamPath.resolve("steamapps").resolve("libraryfolders.vdf")
        try {
            val content = vdfPath.toFile().readText(Charsets.UTF_8)
            val regex = Regex(""""path"\s+"([^"]+)"""")
            for (match in regex.findAll(content)) {
                // VDF 中路径分隔符以 \\ 转义，需要反转义
                val raw = match.groupValues[1].replace("\\\\", "\\")
                raw.toPathOrNull()?.formatted()?.let { path ->
                    if (path != steamPath) libraries.add(path)
                }
            }
        } catch (_: Exception) {
        }
        return libraries
    }

    override fun getGameDataPath(gameName: String): Path? {
        if (gameName.isEmpty()) return null
        return resolveGameDataPath(gameName)
    }

    private fun resolveGameDataPath(gameName: String): Path? {
        // 实际上应基于 `launcher-settings.json` 中的 `gameDataPath`
        return when (OS.CURRENT) {
            OS.Windows -> resolveGameDataPathForWindows(gameName)
            else -> resolveGameDataPathForLinux(gameName)
        }
    }

    private fun resolveGameDataPathForWindows(gameName: String): Path {
        return PlsPaths.userHome.resolve(Path("Documents", "Paradox Interactive", gameName)).formatted()
    }

    private fun resolveGameDataPathForLinux(gameName: String): Path {
        // 按优先级依次尝试已知的游戏数据目录路径
        val home = PlsPaths.userHome
        val candidates = listOf(
            home.resolve(Path(".local", "share", "Paradox Interactive", gameName)),
            home.resolve(Path("Documents", "Paradox Interactive", gameName)),
            home.resolve(Path(".paradoxinteractive", gameName)),
        )
        val result = candidates.firstOrNull { it.isDirectory() } ?: candidates.first()
        return result.formatted()
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

    override fun dispose() {
        // 避免内存泄露
        steamPathCache.clear()
    }
}
