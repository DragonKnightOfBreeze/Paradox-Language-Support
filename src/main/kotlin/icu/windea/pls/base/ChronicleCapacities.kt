package icu.windea.pls.base

import icu.windea.pls.core.isClassPresent

object ChronicleCapacities {
    /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
    fun includeSqlite() = "org.sqlite.JDBC".isClassPresent()

    /** 是否记录缓存的统计数据。 */
    fun recordCacheStats() = System.getProperty("chronicle.capacities.recordCacheStats").toBoolean()

    /** 是否记录索引的统计数据。 */
    fun recordIndexStats() = System.getProperty("chronicle.capacities.recordIndexStats").toBoolean()

    /** 是否在打开项目后，刷新内置规则文件（仅限一次）。 */
    fun refreshBuiltInConfigDirectories() = System.getProperty("chronicle.capacities.refreshBuiltInConfigDirectories").toBoolean()

    /** 处理成员规则的选项数据时，是否保留选项规则列表到其用户数据中（默认仅为内部规则保留）。 */
    fun keepOptionConfigs() = System.getProperty("chronicle.capacities.keepOptionConfigs").toBoolean()
}
