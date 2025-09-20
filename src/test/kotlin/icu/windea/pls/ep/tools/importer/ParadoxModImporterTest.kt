package icu.windea.pls.ep.tools.importer

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.PlsDataProvider
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import icu.windea.pls.test.AssumePredicates
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.nio.charset.StandardCharsets
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

    @Test
    fun importLauncherJsonV2() {
        // 将资源文件拷贝到临时位置
        val ins = javaClass.getResourceAsStream("/tools/playlist_v2.json")
        requireNotNull(ins) { "Missing resource: /tools/playlist_v2.json" }
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
    fun importLauncherDbV2() {
        // 用 V2 脚本初始化临时 DB
        val sqlIns = javaClass.getResourceAsStream("/tools/sqlite_v2.sql")
        requireNotNull(sqlIns) { "Missing resource: /tools/sqlite_v2.sql" }
        val sql = sqlIns.reader(StandardCharsets.UTF_8).readText()

        val outDir = Path.of("build", "tmp", "import-db").also { if (!it.exists()) it.createDirectories() }
        val dbFile = outDir.resolve("launcher_v2_import_${UUID.randomUUID()}.sqlite")

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

        val importer = ParadoxLauncherDbImporter()
        val modSet = ParadoxModSetInfo(gameType, "ImporterTest", emptyList())
        val result = runBlocking { importer.execute(dbFile, modSet) }

        assertEquals(3, result.total)
        assertActualTotal(result.actualTotal)
    }

    private fun assertActualTotal(actualTotal: Int) {
        assertEquals(3, actualTotal) // 目前要求这3个流行模组全部被成功导入
    }
}
