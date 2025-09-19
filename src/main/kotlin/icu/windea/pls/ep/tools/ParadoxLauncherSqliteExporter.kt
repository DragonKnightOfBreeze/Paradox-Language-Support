package icu.windea.pls.ep.tools

import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import org.ktorm.database.Database
import java.nio.file.Path
import java.sql.Connection
import java.sql.Statement

/**
 * 将模组配置导出到 Paradox Launcher 的 SQLite 数据库（最小实现，v2 表结构）。
 *
 * 简介：
 * - 以最小表结构（`Playsets/PlaysetsMods/Mods`）对启动器数据库进行写入，重建集合并同步启用的模组列表。
 * - 仅在类路径存在 `sqlite-jdbc` 与 `ktorm` 时可用（见 [isAvailable]）。
 *
 * 表结构（最小字段集）：
 * - `Playsets(Id, Name, IsActive)`
 * - `PlaysetsMods(PlaysetId, ModId, Position, Enabled)`
 * - `Mods(Id, GameRegistryId, DirPath, DisplayName)`
 *
 * 事务流程：
 * 1) `ensureSchema` 确保三张表存在；
 * 2) `recreatePlayset` 置非活动并删除同名集合后新增集合，并设置为活动；
 * 3) `upsertMods` 依据 `DisplayName+DirPath` upsert 模组，返回对应的 ModId 列表；
 * 4) `linkMods` 清空集合关联并按序写入 `PlaysetsMods(Enabled=1)`。
 *
 * 注意：
 * - 此实现为最小可用版本，不处理迁移历史与更复杂的字段（如 Tags/Status 等），后续可按需扩展。
 */
class ParadoxLauncherSqliteExporter : ParadoxModExporter.SqliteBased {
    companion object {
        private const val DB_NAME = "launcher-v2.sqlite"
    }

    override val text: String = PlsBundle.message("mod.exporter.launcher")

    override fun isAvailable(): Boolean = try {
        Class.forName("org.sqlite.JDBC")
        Class.forName("org.ktorm.database.Database")
        true
    } catch (_: Throwable) {
        false
    }

    override fun defaultDbPath(gameDataPath: Path): Path? = gameDataPath.resolve(DB_NAME)

    override fun exportToDatabase(dbPath: Path, gameId: String, collectionName: String, mods: List<ParadoxModInfo>) {
        if (!isAvailable()) error("SQLite/Ktorm is not available in classpath.")
        val url = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
        val db = Database.connect(url)
        db.useConnection { conn ->
            conn.autoCommit = false
            try {
                ensureSchema(conn)
                val playsetId = recreatePlayset(conn, collectionName)
                val modIds = upsertMods(conn, mods)
                linkMods(conn, playsetId, modIds)
                conn.commit()
            } catch (t: Throwable) {
                conn.rollback()
                throw t
            }
        }
    }

    private fun ensureSchema(conn: Connection) {
        conn.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS Playsets (
                  Id INTEGER PRIMARY KEY,
                  Name TEXT,
                  IsActive INTEGER
                );
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS PlaysetsMods (
                  PlaysetId INTEGER,
                  ModId INTEGER,
                  Position INTEGER,
                  Enabled INTEGER
                );
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS Mods (
                  Id INTEGER PRIMARY KEY,
                  GameRegistryId TEXT,
                  DirPath TEXT,
                  DisplayName TEXT
                );
                """.trimIndent()
            )
        }
    }

    private fun recreatePlayset(conn: Connection, name: String): Int {
        conn.createStatement().use { st ->
            st.executeUpdate("UPDATE Playsets SET IsActive = 0")
            st.executeUpdate("DELETE FROM Playsets WHERE Name = '" + name.replace("'", "''") + "'")
            st.executeUpdate("INSERT INTO Playsets(Name, IsActive) VALUES('" + name.replace("'", "''") + "', 1)")
        }
        conn.createStatement().use { st ->
            val rs = st.executeQuery("SELECT Id FROM Playsets WHERE Name = '" + name.replace("'", "''") + "' ORDER BY Id DESC LIMIT 1")
            rs.use { r -> if (r.next()) return r.getInt(1) }
        }
        error("Failed to create playset")
    }

    private fun upsertMods(conn: Connection, mods: List<ParadoxModInfo>): List<Int> {
        val modIds = ArrayList<Int>(mods.size)
        for (m in mods) {
            val display = (m.name ?: m.modDirectory?.fileName?.toString() ?: "").replace("'", "''")
            val dir = (m.modDirectory?.toString() ?: "").replace("'", "''")
            val gameRegistryId = (m.remoteId ?: "").replace("'", "''")
            var id: Int? = null
            conn.createStatement().use { st ->
                val rs = st.executeQuery("SELECT Id FROM Mods WHERE DisplayName = '$display' AND DirPath = '$dir' LIMIT 1")
                rs.use { r -> if (r.next()) id = r.getInt(1) }
            }
            if (id == null) {
                conn.createStatement().use { st ->
                    st.executeUpdate("INSERT INTO Mods(DisplayName, DirPath, GameRegistryId) VALUES('$display', '$dir', '$gameRegistryId')")
                }
                conn.createStatement().use { st ->
                    val rs = st.executeQuery("SELECT Id FROM Mods WHERE DisplayName = '$display' AND DirPath = '$dir' ORDER BY Id DESC LIMIT 1")
                    rs.use { r -> if (r.next()) id = r.getInt(1) }
                }
            }
            modIds += id ?: error("Failed to upsert mod: $display")
        }
        return modIds
    }

    private fun linkMods(conn: Connection, playsetId: Int, modIds: List<Int>) {
        conn.createStatement().use { st ->
            st.executeUpdate("DELETE FROM PlaysetsMods WHERE PlaysetId = $playsetId")
        }
        for ((index, modId) in modIds.withIndex()) {
            conn.createStatement().use { st: Statement ->
                st.executeUpdate("INSERT INTO PlaysetsMods(PlaysetId, ModId, Position, Enabled) VALUES($playsetId, $modId, $index, 1)")
            }
        }
    }
}
