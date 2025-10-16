package icu.windea.pls.ep.tools.importer

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.PlsDataProvider
import icu.windea.pls.lang.util.PlsSqliteManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import icu.windea.pls.test.AssumePredicates
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxModImporterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private val gameType = ParadoxGameType.Stellaris

    @Before
    fun setup() {
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

    @Test
    fun importLauncherJsonV2() {
        // 将资源文件拷贝到临时位置
        val ins = getResource("/tools/playlist_v2.json")

        val outDir = Path.of("build", "tmp", "import-in").also { if (!it.exists()) it.createDirectories() }
        val jsonFile = outDir.resolve("playlist_v2_import_${UUID.randomUUID()}.json")
        Files.copy(ins, jsonFile)

        val importer = ParadoxLauncherJsonImporter()
        val modSet = ParadoxModSetInfo(gameType, "ImporterTest", emptyList())
        val result = runBlocking { importer.execute(jsonFile, modSet) }

        assertEquals(3, result.total)
        assertActualTotal(result.actualTotal)
    }

    @Test
    fun importLauncherJsonV3() {
        // 将资源文件拷贝到临时位置
        val ins = getResource("/tools/playlist_v3.json")

        val outDir = Path.of("build", "tmp", "import-in").also { if (!it.exists()) it.createDirectories() }
        val jsonFile = outDir.resolve("playlist_v3_import_${UUID.randomUUID()}.json")
        Files.copy(ins, jsonFile)

        val importer = ParadoxLauncherJsonImporter()
        val modSet = ParadoxModSetInfo(gameType, "ImporterTest", emptyList())
        val result = runBlocking { importer.execute(jsonFile, modSet) }

        assertEquals(3, result.total)
        assertActualTotal(result.actualTotal)
    }

    @Test
    fun importLauncherDbV2() {
        // 用 V2 脚本初始化临时 DB
        val sqlIns = getResource("/tools/sqlite_v2.sql")
        val sql = sqlIns.reader().readText()

        val outDir = Path.of("build", "tmp", "import-db").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_v2_import_${UUID.randomUUID()}.sqlite")
        PlsSqliteManager.executeSql(dbFile, sql)

        val importer = ParadoxLauncherDbImporter()
        val modSet = ParadoxModSetInfo(gameType, "ImporterTest", emptyList())
        val result = runBlocking { importer.execute(dbFile, modSet) }

        assertEquals(3, result.total)
        assertActualTotal(result.actualTotal)
    }

    @Test
    fun importLauncherDbV4() {
        // 用 V4 脚本初始化临时 DB
        val sqlIns = getResource("/tools/sqlite_v4.sql")
        val sql = sqlIns.reader().readText()

        val outDir = Path.of("build", "tmp", "import-db").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_v4_import_${UUID.randomUUID()}.sqlite")
        PlsSqliteManager.executeSql(dbFile, sql)

        val importer = ParadoxLauncherDbImporter()
        val modSet = ParadoxModSetInfo(gameType, "ImporterTest", emptyList())
        val result = runBlocking { importer.execute(dbFile, modSet) }

        assertEquals(3, result.total)
        assertActualTotal(result.actualTotal)
    }

    private fun getResource(jsonPath: String): InputStream {
        val ins = javaClass.getResourceAsStream(jsonPath)
        requireNotNull(ins) { "Missing resource: $jsonPath" }
        return ins
    }

    private fun assertActualTotal(actualTotal: Int) {
        assertEquals(3, actualTotal) // 目前要求这3个流行模组全部被成功导入
    }
}
