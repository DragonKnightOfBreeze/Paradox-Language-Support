package icu.windea.pls.ep.tools

import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.nio.file.Files
import java.sql.DriverManager

class ParadoxModImporterSqliteTest {
    @Test
    fun testImportFromDatabase_basic() {
        val tempDb = Files.createTempFile("launcher", ".sqlite")
        tempDb.toFile().deleteOnExit()
        val url = "jdbc:sqlite:${tempDb.toAbsolutePath()}"
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { st ->
                st.executeUpdate("""
                    CREATE TABLE Playsets (
                      Id INTEGER PRIMARY KEY,
                      Name TEXT,
                      IsActive INTEGER
                    );
                """.trimIndent())
                st.executeUpdate("""
                    CREATE TABLE PlaysetsMods (
                      PlaysetId INTEGER,
                      ModId INTEGER,
                      Position INTEGER,
                      Enabled INTEGER
                    );
                """.trimIndent())
                st.executeUpdate("""
                    CREATE TABLE Mods (
                      Id INTEGER PRIMARY KEY,
                      GameRegistryId TEXT,
                      DirPath TEXT,
                      DisplayName TEXT
                    );
                """.trimIndent())
            }
            conn.createStatement().use { st ->
                st.executeUpdate("INSERT INTO Playsets(Id, Name, IsActive) VALUES(1, 'TestPlayset', 1)")
                st.executeUpdate("INSERT INTO Mods(Id, GameRegistryId, DirPath, DisplayName) VALUES(1, 'steam:1', 'C:/mods/m1', 'Mod A')")
                st.executeUpdate("INSERT INTO Mods(Id, GameRegistryId, DirPath, DisplayName) VALUES(2, NULL, 'C:/mods/m2', 'Mod B')")
                st.executeUpdate("INSERT INTO PlaysetsMods(PlaysetId, ModId, Position, Enabled) VALUES(1, 2, 0, 1)")
                st.executeUpdate("INSERT INTO PlaysetsMods(PlaysetId, ModId, Position, Enabled) VALUES(1, 1, 1, 1)")
                st.executeUpdate("INSERT INTO PlaysetsMods(PlaysetId, ModId, Position, Enabled) VALUES(1, 1, 2, 0)") // disabled
            }
        }

        val importer = ParadoxLauncherImporter()
        assumeTrue("SQLite/Ktorm not available", importer.isAvailable())
        val result = importer.importFromDatabase(tempDb)

        assertNull(result.gameId)
        assertEquals("TestPlayset", result.collectionName)
        assertEquals(2, result.mods.size)
        assertEquals("Mod B", result.mods[0].name) // position 0 first
        assertEquals("Mod A", result.mods[1].name)
        // 路径分隔符在 Windows 下为 '\\'，在 *nix 下为 '/': 统一归一化后断言
        val mod2Path = result.mods[0].modDirectory?.toString()?.replace('\\', '/')
        assertEquals("C:/mods/m2", mod2Path)
        assertEquals("steam:1", result.mods[1].remoteId)
        assertTrue(result.mods.all { it.enabled })
    }
}
