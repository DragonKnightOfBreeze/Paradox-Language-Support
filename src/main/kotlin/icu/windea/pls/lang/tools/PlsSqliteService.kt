package icu.windea.pls.lang.tools

import icu.windea.pls.lang.tools.impl.PlsSqliteServiceImpl
import org.ktorm.database.Database
import java.nio.file.Path

interface PlsSqliteService {
    fun executeSql(dbPath: Path, sql: String)

    fun executeSql(db: Database, sql: String)

    companion object : PlsSqliteService by PlsSqliteServiceImpl()
}
