package icu.windea.pls.lang.tools

import com.intellij.openapi.components.serviceOrNull
import org.ktorm.database.Database
import java.nio.file.Path

interface PlsSqliteService {
    fun executeSql(dbPath: Path, sql: String)

    fun executeSql(db: Database, sql: String)

    companion object {
        @JvmStatic
        fun getInstance(): PlsSqliteService = serviceOrNull() ?: PlsSqliteServiceImpl()
    }
}
