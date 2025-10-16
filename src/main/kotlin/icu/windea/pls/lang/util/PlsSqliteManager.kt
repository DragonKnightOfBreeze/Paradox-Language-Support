package icu.windea.pls.lang.util

import org.ktorm.database.Database
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.Statement
import java.util.*

object PlsSqliteManager {
    fun executeSql(dbPath: Path, sql: String) {
        val conn = DriverManager.getConnection("jdbc:sqlite:${dbPath.toAbsolutePath()}")
        conn.autoCommit = false
        conn.createStatement().use { executeSql(it, sql) }
        conn.commit()
        conn.close()
    }

    fun executeSql(db: Database, sql: String) {
        db.useConnection { conn ->
            conn.autoCommit = false
            conn.createStatement().use { executeSql(it, sql) }
            conn.commit()
            conn.autoCommit = true
        }
    }

    @Suppress("SqlSourceToSinkFlow")
    private fun executeSql(statement: Statement, sql: String) {
        // 简单按 ';' 分割执行；自行管理事务，忽略脚本中的 BEGIN/COMMIT 语句
        val stmts = sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }
        for (stmt in stmts) {
            val upper = stmt.uppercase(Locale.ROOT)
            if (upper == "BEGIN" || upper == "BEGIN TRANSACTION" || upper == "COMMIT") continue
            statement.execute(stmt)
        }
    }
}
