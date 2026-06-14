package icu.windea.pls.lang.tools

import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class SpecialPathServiceTest {
    private val service = SpecialPathServiceImpl()
    private val gameTypes = ParadoxGameType.getAll()

    // 测试用模组信息：UI Overhaul Dynamic
    private val testModName = "UI Overhaul Dynamic"
    private val testModSteamId = "1623423360"
    // Steam 创意工坊本地安装路径（若本机未安装则跳过断言）
    private val testModWorkshopPath = "D:/Program Files/Steam/steamapps/workshop/content/281990/$testModSteamId"
    // 游戏数据目录下的本地 .mod 描述符路径
    private val testModLocalDescriptor =
        "${System.getProperty("user.home")}/Documents/Paradox Interactive/Stellaris/mod/ugc_${testModSteamId}.mod"

    // region 辅助方法

    /**
     * 打印路径条目，格式化输出名称、辅助 ID、路径，以及本地存在性。
     */
    private fun printPathEntry(label: String, id: String?, path: Any?, existsStr: String) {
        val idPart = if (id != null) " [$id]" else ""
        println("  $label$idPart: $path  ($existsStr)")
    }

    private fun existsStr(path: java.nio.file.Path?): String {
        if (path == null) return "null"
        return when {
            path.isRegularFile() -> "file, exists"
            path.isDirectory() -> "directory, exists"
            else -> "not exists"
        }
    }

    // endregion

    // region Steam 路径

    @Test
    fun getSteamPath() {
        println("=== Steam Path ===")
        val path = service.getSteamPath()
        printPathEntry("Steam", null, path, existsStr(path))
    }

    @Test
    fun getSteamGamePath() {
        println("=== Steam Game Paths ===")
        for (gameType in gameTypes) {
            val path = service.getSteamGamePath(gameType.steamId, gameType.title)
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    @Test
    fun getSteamGameWorkshopPath() {
        println("=== Steam Game Workshop Paths ===")
        for (gameType in gameTypes) {
            val path = service.getSteamGameWorkshopPath(gameType.steamId)
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    // endregion

    // region 游戏数据路径

    @Test
    fun getGameDataPath() {
        println("=== Game Data Paths ===")
        for (gameType in gameTypes) {
            val path = service.getGameDataPath(gameType)
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    @Test
    fun getGameModDirectory() {
        println("=== Game Mod Directories (gameData/mod) ===")
        for (gameType in gameTypes) {
            val gameDataPath = service.getGameDataPath(gameType)
            val path = gameDataPath?.resolve("mod")
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    @Test
    fun getGameSaveGames() {
        println("=== Game Save Games Directories (gameData/save games) ===")
        for (gameType in gameTypes) {
            val gameDataPath = service.getGameDataPath(gameType)
            val path = gameDataPath?.resolve("save games")
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    @Test
    fun getGameLogs() {
        println("=== Game Log Directories (gameData/logs) ===")
        for (gameType in gameTypes) {
            val gameDataPath = service.getGameDataPath(gameType)
            val path = gameDataPath?.resolve("logs")
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    // endregion

    // region 游戏可执行文件路径

    @Test
    fun getGameExecutablePath() {
        println("=== Game Executable Paths ===")
        for (gameType in gameTypes) {
            val rootPath = service.getSteamGamePath(gameType.steamId, gameType.title)
            if (rootPath == null) {
                printPathEntry(gameType.title, gameType.id, null, "null (Steam game path not found)")
                continue
            }
            val path = ParadoxGameManager.getExecutablePath(gameType, rootPath)
            printPathEntry(gameType.title, gameType.id, path, existsStr(path))
        }
    }

    // endregion

    // region 模组路径（UI Overhaul Dynamic）

    @Test
    fun getModWorkshopPath() {
        println("=== Mod Workshop Path: $testModName ===")
        val path = service.getSteamGameWorkshopPath("281990") // Stellaris steamId
        val modPath = path?.resolve(testModSteamId)
        printPathEntry(testModName, testModSteamId, modPath, existsStr(modPath))
        // 本地路径不存在不断言失败
    }

    @Test
    fun getModLocalDescriptorPath() {
        println("=== Mod Local Descriptor Path: $testModName ===")
        val path = kotlin.io.path.Path(testModLocalDescriptor)
        printPathEntry("$testModName (.mod)", testModSteamId, path, existsStr(path))
        // 本地路径不存在不断言失败
    }

    // endregion
}
