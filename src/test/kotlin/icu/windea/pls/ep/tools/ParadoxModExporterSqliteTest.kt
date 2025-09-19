package icu.windea.pls.ep.tools

import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager

class ParadoxModExporterSqliteTest {
    @Test
    fun testExportToDatabase_basic() {
        val tempDb = Files.createTempFile("launcher_export", ".sqlite")
        tempDb.toFile().deleteOnExit()

        val exporter = ParadoxLauncherSqliteExporter()
        assumeTrue("SQLite/Ktorm not available", exporter.isAvailable())

        val mods = listOf(
            ParadoxModInfo(name = "A", modDirectory = Paths.get("C:/mods/a"), source = ParadoxModSource.Steam, remoteId = "1", enabled = true),
            ParadoxModInfo(name = "B", modDirectory = Paths.get("C:/mods/b"), source = ParadoxModSource.Paradox, remoteId = "2", enabled = true)
        )
        exporter.exportToDatabase(tempDb, "stellaris", "ExportedPlayset", mods)

        val url = "jdbc:sqlite:${tempDb.toAbsolutePath()}"
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT COUNT(*) FROM Playsets").use { rs ->
                    assertTrue(rs.next()); assertEquals(1, rs.getInt(1))
                }
                st.executeQuery("SELECT Name, IsActive FROM Playsets").use { rs ->
                    assertTrue(rs.next()); assertEquals("ExportedPlayset", rs.getString(1)); assertEquals(1, rs.getInt(2))
                }
                st.executeQuery("SELECT COUNT(*) FROM Mods").use { rs ->
                    assertTrue(rs.next()); assertEquals(2, rs.getInt(1))
                }
                st.executeQuery("SELECT COUNT(*) FROM PlaysetsMods").use { rs ->
                    assertTrue(rs.next()); assertEquals(2, rs.getInt(1))
                }
                st.executeQuery("SELECT Position FROM PlaysetsMods ORDER BY Position").use { rs ->
                    assertTrue(rs.next()); assertEquals(0, rs.getInt(1))
                    assertTrue(rs.next()); assertEquals(1, rs.getInt(1))
                }
            }
        }
    }
}
