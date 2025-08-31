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
 * Simple SQLite-backed key-value store for profile settings using Ktorm.
 *
 * Schema: single table with category + key + xml payload.
 * This design keeps refactor small by storing XML serialized state values as TEXT.
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
                    CREATE TABLE IF NOT EXISTS profile_entries (
                      category TEXT NOT NULL,
                      key_path TEXT NOT NULL,
                      value_xml TEXT NOT NULL,
                      updated_at INTEGER NOT NULL,
                      PRIMARY KEY(category, key_path)
                    );
                    """.trimIndent()
                )
            }
        }
    }

    object ProfileEntries : Table<Nothing>("profile_entries") {
        val category = varchar("category")
        val keyPath = varchar("key_path")
        val valueXml = text("value_xml")
        val updatedAt = long("updated_at")
    }

    fun get(category: String, key: String): String? {
        val db = ensureDatabase()
        return db.from(ProfileEntries)
            .select(ProfileEntries.valueXml)
            .where { (ProfileEntries.category eq category) and (ProfileEntries.keyPath eq key) }
            .map { it[ProfileEntries.valueXml] }
            .firstOrNull()
    }

    fun put(category: String, key: String, xml: String) {
        val db = ensureDatabase()
        val now = Instant.now().toEpochMilli()
        db.useConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO profile_entries(category, key_path, value_xml, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(category, key_path) DO UPDATE SET
                  value_xml = excluded.value_xml,
                  updated_at = excluded.updated_at
                ;
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, category)
                ps.setString(2, key)
                ps.setString(3, xml)
                ps.setLong(4, now)
                ps.executeUpdate()
            }
        }
    }

    fun remove(category: String, key: String): Boolean {
        val db = ensureDatabase()
        val affected = db.delete(ProfileEntries) { (it.category eq category) and (it.keyPath eq key) }
        return affected > 0
    }

    fun clear(category: String) {
        val db = ensureDatabase()
        db.delete(ProfileEntries) { it.category eq category }
    }

    fun keys(category: String): Set<String> {
        val db = ensureDatabase()
        return db.from(ProfileEntries)
            .select(ProfileEntries.keyPath)
            .where { ProfileEntries.category eq category }
            .mapNotNull { it[ProfileEntries.keyPath] }
            .toSet()
    }

    fun entries(category: String): List<Pair<String, String>> {
        val db = ensureDatabase()
        return db.from(ProfileEntries)
            .select(ProfileEntries.keyPath, ProfileEntries.valueXml)
            .where { ProfileEntries.category eq category }
            .map { (it[ProfileEntries.keyPath]!! to it[ProfileEntries.valueXml]!!) }
    }
}
