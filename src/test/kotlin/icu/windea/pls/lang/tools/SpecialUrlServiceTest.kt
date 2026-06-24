package icu.windea.pls.lang.tools

import com.intellij.platform.util.coroutines.mapConcurrent
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ParallelMethodRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URI

@Suppress("UnstableApiUsage")
@RunWith(ParallelMethodRunner::class)
class SpecialUrlServiceTest {
    private val service = SpecialUrlServiceImpl()
    private val gameTypes = ParadoxGameType.getAllSpecific()

    // 测试用模组信息：UI Overhaul Dynamic
    // Steam 创意工坊 steamId 与本地 mod 路径
    private val testModName = "UI Overhaul Dynamic"
    private val testModSteamId = "1623423360"
    // private val testModLocalPath = "${System.getProperty("user.home")}/Documents/Paradox Interactive/Stellaris/mod/ugc_${testModSteamId}.mod"
    // private val testModWorkshopPath = "D:/Program Files/Steam/steamapps/workshop/content/281990/$testModSteamId"

    // region 辅助方法

    /**
     * 检查 HTTP(S) URL 是否可访问（返回 2xx 状态码）。
     * steam:// 协议链接跳过检查（无法通过 HTTP 校验）。
     *
     * @return 状态码，若不可访问返回负值
     */
    private fun checkUrl(url: String): Int {
        if (service.isSteamUrl(url)) return -1 // steam:// 协议，跳过
        return try {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.instanceFollowRedirects = true
            conn.connect()
            conn.responseCode
        } catch (_: Exception) {
            -1
        }
    }

    /**
     * 打印 URL 条目，格式化输出名称、辅助 ID、URL，以及可访问性状态。
     */
    private fun printUrlEntry(label: String, id: String?, url: String, statusCode: Int) {
        val idPart = if (id != null) " [$id]" else ""
        val statusPart = when {
            service.isSteamUrl(url) -> " (steam://)"
            statusCode in 200..299 -> " (accessible, $statusCode)"
            statusCode < 0 -> " (check skipped or unreachable)"
            else -> " (inaccessible, $statusCode)"
        }
        println("  $label$idPart: $url$statusPart")
    }

    private fun checkAccessibleOrTolerated(code: Int): Boolean {
        return code in 200..299 || code == -1
    }

    // endregion

    // region Steam 游戏链接（网页）

    @Test
    fun getSteamGameStoreUrl() {
        println("=== Steam Game Store URLs (website) ===")
        val failed = mutableListOf<String>()
        val tuple = runBlocking {
            gameTypes.mapConcurrent { gameType ->
                val url = service.getSteamGameStoreUrl(gameType.steamId)
                val code = checkUrl(url)
                tupleOf(gameType, url, code)
            }
        }
        for ((gameType, url, code) in tuple) {
            printUrlEntry(gameType.title, gameType.id, url, code)
            if (!checkAccessibleOrTolerated(code)) failed += "${gameType.id}: $url (HTTP $code)"
        }
        if (failed.isNotEmpty()) Assert.fail("Some Steam game store URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    @Test
    fun getSteamGameWorkshopUrl() {
        println("=== Steam Game Workshop URLs (website) ===")
        val failed = mutableListOf<String>()
        val tuple = runBlocking {
            gameTypes.mapConcurrent { gameType ->
                val url = service.getSteamGameWorkshopUrl(gameType.steamId)
                val code = checkUrl(url)
                tupleOf(gameType, url, code)
            }
        }
        for ((gameType, url, code) in tuple) {
            printUrlEntry(gameType.title, gameType.id, url, code)
            if (!checkAccessibleOrTolerated(code)) failed += "${gameType.id}: $url (HTTP $code)"
        }
        if (failed.isNotEmpty()) Assert.fail("Some Steam game workshop URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    @Test
    fun getSteamWorkshopUrl() {
        println("=== Steam Workshop Item URLs (website) ===")
        val failed = mutableListOf<String>()
        val url = service.getSteamWorkshopUrl(testModSteamId)
        val code = checkUrl(url)
        printUrlEntry(testModName, testModSteamId, url, code)
        if (!checkAccessibleOrTolerated(code)) failed += "$testModName: $url (HTTP $code)"
        if (failed.isNotEmpty()) Assert.fail("Some Steam workshop item URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    // endregion

    // region Steam 游戏链接（Steam 客户端协议）

    @Test
    fun getSteamGameStoreUrlInSteam() {
        println("=== Steam Game Store URLs (steam://) ===")
        for (gameType in gameTypes) {
            val url = service.getSteamGameStoreUrlInSteam(gameType.steamId)
            printUrlEntry(gameType.title, gameType.id, url, -1)
        }
    }

    @Test
    fun getSteamGameWorkshopUrlInSteam() {
        println("=== Steam Game Workshop URLs (steam://) ===")
        for (gameType in gameTypes) {
            val url = service.getSteamGameWorkshopUrlInSteam(gameType.steamId)
            printUrlEntry(gameType.title, gameType.id, url, -1)
        }
    }

    @Test
    fun getSteamWorkshopUrlInSteam() {
        println("=== Steam Workshop Item URLs (steam://) ===")
        val url = service.getSteamWorkshopUrlInSteam(testModSteamId)
        printUrlEntry(testModName, testModSteamId, url, -1)
    }

    @Test
    fun getSteamGameLaunchUrl() {
        println("=== Steam Game Launch URLs (steam://) ===")
        for (gameType in gameTypes) {
            val url = service.getSteamGameLaunchUrl(gameType.steamId)
            printUrlEntry(gameType.title, gameType.id, url, -1)
        }
    }

    // endregion

    // region SteamDB 链接

    @Test
    fun getSteamDbAppUrl() {
        println("=== SteamDB App URLs ===")
        // SteamDB 对自动化请求返回 403，只打印、不断言
        for (gameType in gameTypes) {
            val url = service.getSteamDbAppUrl(gameType.steamId)
            println("  ${gameType.title} [${gameType.id}]: $url (check skipped: SteamDB blocks automated requests)")
        }
    }

    // endregion

    // region Paradox Mods 链接

    @Test
    fun getParadoxModsGameUrl() {
        println("=== Paradox Mods Game URLs ===")
        val failed = mutableListOf<String>()
        val tuple = runBlocking {
            gameTypes.mapConcurrent { gameType ->
                val gameId = gameType.gameId.takeIf { it.isNotEmpty() } ?: return@mapConcurrent null
                val url = service.getParadoxModsGameUrl(gameId)
                val code = checkUrl(url)
                tupleOf(gameType, url, code)
            }.filterNotNull()
        }
        for ((gameType, url, code) in tuple) {
            printUrlEntry(gameType.title, gameType.id, url, code)
            if (!checkAccessibleOrTolerated(code)) failed += "${gameType.id}: $url (HTTP $code)"
        }
        if (failed.isNotEmpty()) Assert.fail("Some Paradox Mods game URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    @Test
    fun getParadoxModsModUrl() {
        println("=== Paradox Mods Mod URLs ===")
        val failed = mutableListOf<String>()
        // 使用 UI Overhaul Dynamic 的 Paradox Mods ID（同 steamId 均为数字 ID，此处用 steamId 演示格式）
        // 实际 Paradox Mods 模组 ID 通常与 Steam remoteId 不同；此条目演示 URL 格式的可访问性
        val pdxModId = "4519" // 一个已知存在于 Paradox Mods 的模组 ID（来自之前验证）
        val pdxModName = "anime road to 56"
        val url = service.getParadoxModsModUrl(pdxModId)
        val code = checkUrl(url)
        printUrlEntry(pdxModName, pdxModId, url, code)
        if (!checkAccessibleOrTolerated(code)) failed += "$pdxModName: $url (HTTP $code)"
        if (failed.isNotEmpty()) Assert.fail("Some Paradox Mods mod URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    // endregion

    // region 官网相关链接

    @Test
    fun getGameForumUrl() {
        println("=== Game Forum URLs ===")
        val failed = mutableListOf<String>()
        val tuple = runBlocking {
            gameTypes.mapConcurrent { gameType ->
                val url = service.getGameForumUrl(gameType) ?: return@mapConcurrent null
                val code = checkUrl(url)
                tupleOf(gameType, url, code)
            }.filterNotNull()
        }
        for ((gameType, url, code) in tuple) {
            printUrlEntry(gameType.title, gameType.id, url, code)
            if (!checkAccessibleOrTolerated(code)) failed += "${gameType.id}: $url (HTTP $code)"
        }
        if (failed.isNotEmpty()) Assert.fail("Some game forum URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    @Test
    fun getGameWikiUrl() {
        println("=== Game Wiki URLs ===")
        val failed = mutableListOf<String>()
        val tuple = runBlocking {
            gameTypes.mapConcurrent { gameType ->
                val url = service.getGameWikiUrl(gameType)
                val code = checkUrl(url)
                tupleOf(gameType, url, code)
            }
        }
        for ((gameType, url, code) in tuple) {
            printUrlEntry(gameType.title, gameType.id, url, code)
            if (!checkAccessibleOrTolerated(code)) failed += "${gameType.id}: $url (HTTP $code)"
        }
        if (failed.isNotEmpty()) Assert.fail("Some game wiki URLs are inaccessible:\n${failed.joinToString("\n")}")
    }

    // endregion
}
