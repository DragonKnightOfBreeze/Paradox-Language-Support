package icu.windea.pls.ep.tools.exporter

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.ep.tools.model.Playsets
import icu.windea.pls.ep.tools.model.PlaysetsMods
import icu.windea.pls.lang.PlsDataProvider
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModInfo
import icu.windea.pls.model.tools.ParadoxModSetInfo
import icu.windea.pls.test.AssumePredicates
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxModExporterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private val gameType = ParadoxGameType.Stellaris
    private val remoteIds = listOf("1623423360", "819148835", "937289339") // UIOD, PD, RS

    @Before
    fun before() {
        AssumePredicates.includeLocalEnv()
        addAllowedRoots()
    }

    private fun addAllowedRoots() {
        // com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots
        val dataProvider = PlsDataProvider()
        val additionalAllowedRoots = listOfNotNull(
            dataProvider.getSteamWorkshopPath(gameType.steamId),
            dataProvider.getGameDataPath(gameType.title),
        )
        System.setProperty("vfs.additional-allowed-roots", additionalAllowedRoots.joinToString(File.pathSeparator))
    }

    private fun buildModSetInfoFromWorkshop(): ParadoxModSetInfo {
        val workshop = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
            ?: throw AssertionError("Steam workshop path not found for ${gameType.title}")
        val mods = remoteIds.mapNotNull { id ->
            val dir = ParadoxMetadataManager.getModDirectoryFromSteamId(id, workshop)
            if (dir == null) null else ParadoxModInfo(modDirectory = dir, enabled = true, remoteId = id)
        }
        assertTrue("None of the 3 sample mods are installed in workshop.", mods.isNotEmpty())
        return ParadoxModSetInfo(gameType, "ExporterTest", mods)
    }

    @Test
    fun exportLauncherJsonV2() {
        val outDir = Path.of("build", "tmp", "export-out").also { if (!it.exists()) it.createDirectories() }
        val outFile = outDir.resolve("playlist_v2_out.json")

        val exporter = ParadoxLauncherJsonV2Exporter()
        val modSet = buildModSetInfoFromWorkshop()
        val result = runBlocking { exporter.execute(outFile, modSet) }
        assertActualTotal(result.actualTotal)

        // 验证 JSON 内容
        val json = ObjectMappers.jsonMapper.readValue(Files.newInputStream(outFile), LauncherJsonV2::class.java)
        assertEquals(modSet.gameType.gameId, json.game)
        assertTrue(json.mods.size == result.actualTotal)
        assertTrue(json.mods.all { it.enabled })
        // V2 position：左侧补零十位
        assertTrue(json.mods.all { it.position.length == 10 && it.position.all(Char::isDigit) })
    }

    @Test
    fun exportLauncherDb_v2PositionsOnNewDb() {
        // 用 V2 脚本初始化临时 DB
        val sqlIns = javaClass.getResourceAsStream("/tools/sqlite_v2.sql")
        requireNotNull(sqlIns) { "Missing resource: /tools/sqlite_v2.sql" }
        val sql = sqlIns.reader(StandardCharsets.UTF_8).readText()

        val outDir = Path.of("build", "tmp", "export-out").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_test_${UUID.randomUUID()}.sqlite")

        val conn = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.toAbsolutePath()}")
        conn.autoCommit = false
        conn.createStatement().use { st ->
            sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }.forEach { stmt ->
                val up = stmt.uppercase(Locale.ROOT)
                if (up == "BEGIN" || up == "BEGIN TRANSACTION" || up == "COMMIT") return@forEach
                st.execute(stmt)
            }
        }
        conn.commit()
        conn.close()

        val exporter = ParadoxLauncherDbExporter()
        val modSet = buildModSetInfoFromWorkshop()
        val result = runBlocking { exporter.execute(dbFile, modSet) }
        assertActualTotal(result.actualTotal)

        val db = Database.connect("jdbc:sqlite:${dbFile.toAbsolutePath()}", driver = "org.sqlite.JDBC")
        val playset = db.sequenceOf(Playsets).firstOrNull { Playsets.isActive eq true }
        assertNotNull(playset)
        val mappings = db.sequenceOf(PlaysetsMods).filter { PlaysetsMods.playsetId eq playset!!.id }.toList()
        assertTrue(mappings.isNotEmpty())
        // 新建 DB 没有 V4 迁移标记，应按 V2 写出，position 为 10 位数字字符串
        assertTrue(mappings.all { it.position != null && it.position!!.length == 10 && it.position!!.all(Char::isDigit) })
    }

    private fun assertActualTotal(actualTotal: Int) {
        assertEquals(3, actualTotal) // 目前要求这3个流行模组全部被成功导出
    }
}
