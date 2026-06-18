package icu.windea.pls.base.io

import com.intellij.openapi.components.serviceOrNull
import org.ktorm.database.Database
import java.nio.file.Path

interface ChronicleSqliteService {
    fun executeSql(dbPath: Path, sql: String)

    fun executeSql(db: Database, sql: String)

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleSqliteService = serviceOrNull() ?: ChronicleSqliteServiceImpl()
    }
}
