package icu.windea.pls.base.io

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.ktorm.database.Database
import java.nio.file.Path
import java.sql.DriverManager
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

/**
 * @see ChronicleSqliteService
 * @see ChronicleSqliteServiceImpl
 */
@RunWith(JUnit4::class)
class ChronicleSqliteServiceTest : BasePlatformTestCase() {
    private val service = ChronicleSqliteServiceImpl()

    @Test
    fun executeSql_path_test() {
        val dbPath = testDatabasePath("path.db")
        service.executeSql(
            dbPath,
            """
            CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT);
            INSERT INTO test_table (id, name) VALUES (1, 'a');
            INSERT INTO test_table (id, name) VALUES (2, 'b');
            """.trimIndent()
        )

        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT name FROM test_table ORDER BY id").use { result ->
                    Assert.assertTrue(result.next())
                    Assert.assertEquals("a", result.getString(1))
                    Assert.assertTrue(result.next())
                    Assert.assertEquals("b", result.getString(1))
                    Assert.assertFalse(result.next())
                }
            }
        }
    }

    @Test
    fun executeSql_database_test() {
        val dbPath = testDatabasePath("database.db")
        val database = Database.connect("jdbc:sqlite:$dbPath")
        service.executeSql(
            database,
            """
            BEGIN TRANSACTION;
            CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT);
            INSERT INTO test_table (id, name) VALUES (1, 'a');
            COMMIT;
            """.trimIndent()
        )

        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM test_table").use { result ->
                    Assert.assertTrue(result.next())
                    Assert.assertEquals(1, result.getInt(1))
                }
            }
        }
    }

    private fun testDatabasePath(fileName: String): Path {
        val dir = Path.of("build", "tmp", "base-io-sqlite-test").also { it.createDirectories() }
        return dir.resolve(fileName).also { it.deleteIfExists() }
    }
}
