package icu.windea.pls.ep.tools.exporter

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.jsonMapper
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.ep.tools.model.Playsets
import icu.windea.pls.ep.tools.model.PlaysetsMods
import icu.windea.pls.lang.analysis.ParadoxMetadataManager
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.tools.PlsSqliteService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
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
import java.io.InputStream
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
    fun setup() {
        AssumePredicates.includeLocalEnv()
        addAllowedRoots()
    }

    private fun addAllowedRoots() {
        // com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots
        val additionalAllowedRoots = listOfNotNull(
            PlsPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId),
            PlsPathService.getInstance().getGameDataPath(gameType.title),
        )
        System.setProperty("vfs.additional-allowed-roots", additionalAllowedRoots.joinToString(File.pathSeparator))
    }

    private fun buildModSetInfoFromWorkshop(): ParadoxModSetInfo {
        val workshop = PlsPathService.getInstance().getSteamGameWorkshopPath(gameType.steamId)
            ?: throw AssertionError("Steam workshop path not found for ${gameType.title}")
        val mods = remoteIds.mapNotNull f@{ id ->
            val dir = ParadoxMetadataManager.getModDirectoryFromSteamId(id, workshop) ?: return@f null
            ParadoxModInfo(modDirectory = dir, enabled = true, remoteId = id, source = ParadoxModSource.Steam)
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
        val json = jsonMapper.readValue(Files.newInputStream(outFile), LauncherJsonV2::class.java)
        assertEquals(modSet.gameType.gameId, json.game)
        assertTrue(json.mods.size == result.actualTotal)
        assertTrue(json.mods.all { it.enabled })
        // V2 position：左侧补零十位
        assertTrue(json.mods.all { it.position.length == 10 && it.position.all(Char::isDigit) })
    }

    @Test
    fun exportLauncherJsonV3() {
        val outDir = Path.of("build", "tmp", "export-out").also { if (!it.exists()) it.createDirectories() }
        val outFile = outDir.resolve("playlist_v3_out.json")

        val exporter = ParadoxLauncherJsonV3Exporter()
        val modSet = buildModSetInfoFromWorkshop()
        val result = runBlocking { exporter.execute(outFile, modSet) }
        assertActualTotal(result.actualTotal)

        // 验证 JSON 内容
        val json = jsonMapper.readValue(Files.newInputStream(outFile), LauncherJsonV3::class.java)
        assertEquals(modSet.gameType.gameId, json.game)
        assertTrue(json.mods.size == result.actualTotal)
        assertTrue(json.mods.all { it.enabled })
        // V3 position：从0开始
        assertTrue(json.mods.all { it.position >= 0 })
    }

    @Test
    fun exportLauncherDbV2_onNewDb() {
        // 用 V2 脚本初始化临时 DB
        val sqlIns = getResource("/tools/sqlite_v2_new_db.sql")
        val sql = sqlIns.reader().readText()

        val outDir = Path.of("build", "tmp", "export-out").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_v2_export_${UUID.randomUUID()}.sqlite")

        PlsSqliteService.getInstance().executeSql(dbFile, sql)

        val exporter = ParadoxLauncherDbExporter()
        val modSet = buildModSetInfoFromWorkshop()
        val result = runBlocking { exporter.execute(dbFile, modSet) }
        assertActualTotal(result.actualTotal)

        val db = Database.connect("jdbc:sqlite:${dbFile.toAbsolutePath()}", driver = "org.sqlite.JDBC")
        val playset = db.sequenceOf(Playsets).firstOrNull { it.isActive eq false }
        assertNotNull(playset) // 没有则创建，默认不激活
        val mappings = db.sequenceOf(PlaysetsMods).filter { it.playsetId eq playset!!.id }.toList()
        assertTrue(mappings.isNotEmpty())
        // 新建 DB 没有 V4 迁移标记，应按 V2 写出，position 为 10 位数字字符串
        assertTrue(mappings.all { it.position != null && it.position!!.length == 10 && it.position!!.all(Char::isDigit) })
    }

    @Test
    fun exportLauncherDbV4_onNewDb() {
        // 用 V4 脚本初始化临时 DB
        val sqlIns = getResource("/tools/sqlite_v4_new_db.sql")
        val sql = sqlIns.reader().readText()

        val outDir = Path.of("build", "tmp", "export-out").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_v2_export_${UUID.randomUUID()}.sqlite")

        PlsSqliteService.getInstance().executeSql(dbFile, sql)

        val exporter = ParadoxLauncherDbExporter()
        val modSet = buildModSetInfoFromWorkshop()
        val result = runBlocking { exporter.execute(dbFile, modSet) }
        assertActualTotal(result.actualTotal)

        val db = Database.connect("jdbc:sqlite:${dbFile.toAbsolutePath()}", driver = "org.sqlite.JDBC")
        val playset = db.sequenceOf(Playsets).firstOrNull { it.isActive eq false }
        assertNotNull(playset) // 没有则创建，默认不激活
        val mappings = db.sequenceOf(PlaysetsMods).filter { it.playsetId eq playset!!.id }.toList()
        assertTrue(mappings.isNotEmpty())

        assertTrue(mappings.all { it.position?.toIntOrNull() != null })
    }

    private fun getResource(jsonPath: String): InputStream {
        val ins = javaClass.getResourceAsStream(jsonPath)
        requireNotNull(ins) { "Missing resource: $jsonPath" }
        return ins
    }

    private fun assertActualTotal(actualTotal: Int) {
        assertEquals(3, actualTotal) // 目前要求这3个流行模组全部被成功导出
    }
}
