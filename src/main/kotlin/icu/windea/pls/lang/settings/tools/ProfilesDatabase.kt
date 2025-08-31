package icu.windea.pls.lang.settings.tools

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * 使用 Ktorm 的 SQLite 持久层，存储 Profiles 相关设置。
 *
 * 结构：按分类拆分四张业务表，另加一张 `profile_meta` 元信息表。
 * - profile_game_descriptor(category 固定) / profile_mod_descriptor / profile_game_settings / profile_mod_settings
 *   - key_path TEXT PRIMARY KEY
 *   - value_xml TEXT NOT NULL
 *   - updated_at INTEGER NOT NULL
 * - profile_meta
 *   - key TEXT PRIMARY KEY
 *   - value TEXT NOT NULL
 *
 * 仍然通过 XML（XmlSerializer）序列化值对象，避免频繁变更表结构。
 */
object ProfilesDatabase {
    private const val DEFAULT_DB_FILE_NAME = "profiles.db"

    private val dbRef = AtomicReference<Database?>()
    private val dbPathRef = AtomicReference<Path>()

    // Allow overriding database path in tests via system property or explicit setter
    private const val DB_PATH_SYS_PROP = "pls.database.path"

    fun setDatabasePathForTest(path: Path) {
        dbPathRef.set(path)
        // Ktorm Database does not need explicit close
        dbRef.set(null)
    }

    private fun resolveDbPath(): Path {
        dbPathRef.get()?.let { return it }
        val override = System.getProperty(DB_PATH_SYS_PROP)?.takeIf { it.isNotBlank() }
        if (override != null) return Paths.get(override)
        val userHome = System.getProperty("user.home")
        val base = Paths.get(userHome, ".pls", "database")
        return base.resolve(DEFAULT_DB_FILE_NAME)
    }

    private fun ensureDatabase(): Database {
        dbRef.get()?.let { return it }
        synchronized(this) {
            dbRef.get()?.let { return it }
            val dbPath = resolveDbPath()
            Files.createDirectories(dbPath.parent)
            val url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString()
            val db = Database.connect(url)
            createSchemaIfNeeded(db)
            dbPathRef.set(dbPath)
            dbRef.set(db)
            return db
        }
    }

    private fun createSchemaIfNeeded(db: Database) {
        db.useConnection { conn ->
            conn.createStatement().use { st ->
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS profile_game_descriptor (
                      key_path TEXT PRIMARY KEY,
                      value_xml TEXT NOT NULL,
                      updated_at INTEGER NOT NULL
                    );
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS profile_mod_descriptor (
                      key_path TEXT PRIMARY KEY,
                      value_xml TEXT NOT NULL,
                      updated_at INTEGER NOT NULL
                    );
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS profile_game_settings (
                      key_path TEXT PRIMARY KEY,
                      value_xml TEXT NOT NULL,
                      updated_at INTEGER NOT NULL
                    );
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS profile_mod_settings (
                      key_path TEXT PRIMARY KEY,
                      value_xml TEXT NOT NULL,
                      updated_at INTEGER NOT NULL
                    );
                    """.trimIndent()
                )
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS profile_meta (
                      key TEXT PRIMARY KEY,
                      value TEXT NOT NULL
                    );
                    """.trimIndent()
                )
            }
        }
    }

    private interface ProfileTableDef {
        val table: Table<Nothing>
        val keyPath: Column<String>
        val valueXml: Column<String>
        val updatedAt: Column<Long>
    }

    private object GameDescriptorEntries : Table<Nothing>("profile_game_descriptor"), ProfileTableDef {
        override val table: Table<Nothing> get() = this
        override val keyPath = varchar("key_path")
        override val valueXml = text("value_xml")
        override val updatedAt = long("updated_at")
    }

    private object ModDescriptorEntries : Table<Nothing>("profile_mod_descriptor"), ProfileTableDef {
        override val table: Table<Nothing> get() = this
        override val keyPath = varchar("key_path")
        override val valueXml = text("value_xml")
        override val updatedAt = long("updated_at")
    }

    private object GameSettingsEntries : Table<Nothing>("profile_game_settings"), ProfileTableDef {
        override val table: Table<Nothing> get() = this
        override val keyPath = varchar("key_path")
        override val valueXml = text("value_xml")
        override val updatedAt = long("updated_at")
    }

    private object ModSettingsEntries : Table<Nothing>("profile_mod_settings"), ProfileTableDef {
        override val table: Table<Nothing> get() = this
        override val keyPath = varchar("key_path")
        override val valueXml = text("value_xml")
        override val updatedAt = long("updated_at")
    }

    object ProfileMeta : Table<Nothing>("profile_meta") {
        val key = varchar("key")
        val value = text("value")
    }

    private fun resolve(category: String): ProfileTableDef = when (category) {
        "gameDescriptorSettings" -> GameDescriptorEntries
        "modDescriptorSettings" -> ModDescriptorEntries
        "gameSettings" -> GameSettingsEntries
        "modSettings" -> ModSettingsEntries
        else -> throw IllegalArgumentException("Unknown category: $category")
    }

    fun get(category: String, key: String): String? {
        val db = ensureDatabase()
        val def = resolve(category)
        return db.from(def.table)
            .select(def.valueXml)
            .where { def.keyPath eq key }
            .map { it[def.valueXml] }
            .firstOrNull()
    }

    fun put(category: String, key: String, xml: String) {
        val db = ensureDatabase()
        val def = resolve(category)
        val tableName = def.table.tableName
        val now = Instant.now().toEpochMilli()
        db.useConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO $tableName(key_path, value_xml, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(key_path) DO UPDATE SET
                  value_xml = excluded.value_xml,
                  updated_at = excluded.updated_at
                ;
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, key)
                ps.setString(2, xml)
                ps.setLong(3, now)
                ps.executeUpdate()
            }
        }
    }

    fun remove(category: String, key: String): Boolean {
        val db = ensureDatabase()
        val def = resolve(category)
        val affected = db.delete(def.table) { def.keyPath eq key }
        return affected > 0
    }

    fun clear(category: String) {
        val db = ensureDatabase()
        val def = resolve(category)
        db.delete(def.table) { def.keyPath like "%" } // 删除整表所有行
    }

    fun keys(category: String): Set<String> {
        val db = ensureDatabase()
        val def = resolve(category)
        return db.from(def.table)
            .select(def.keyPath)
            .mapNotNull { it[def.keyPath] }
            .toSet()
    }

    fun entries(category: String): List<Pair<String, String>> {
        val db = ensureDatabase()
        val def = resolve(category)
        return db.from(def.table)
            .select(def.keyPath, def.valueXml)
            .map { (it[def.keyPath]!! to it[def.valueXml]!!) }
    }

    fun getMeta(key: String): String? {
        val db = ensureDatabase()
        return db.from(ProfileMeta)
            .select(ProfileMeta.value)
            .where { ProfileMeta.key eq key }
            .map { it[ProfileMeta.value] }
            .firstOrNull()
    }

    fun putMeta(key: String, value: String) {
        val db = ensureDatabase()
        db.useConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO profile_meta(key, value)
                VALUES(?, ?)
                ON CONFLICT(key) DO UPDATE SET value = excluded.value
                ;
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, key)
                ps.setString(2, value)
                ps.executeUpdate()
            }
        }
    }
}
