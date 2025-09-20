package icu.windea.pls.lang.util

import icu.windea.pls.core.isClassPresent

object PlsSqliteManager {
    fun isAvailable(): Boolean {
        return "org.sqlite.JDBC".isClassPresent() && "org.ktorm.database.Database".isClassPresent()
    }
}
