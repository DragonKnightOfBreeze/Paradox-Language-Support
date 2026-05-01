package icu.windea.pls

import icu.windea.pls.core.isClassPresent

object PlsCapacities {
    /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
    fun includeSqlite() = "org.sqlite.JDBC".isClassPresent()

    /** 是否记录缓存状态。 */
    fun recordCacheStats() = System.getProperty("pls.record.cache.status").toBoolean()

    /** 是否记录索引状态 */
    fun recordIndexStats() = System.getProperty("pls.record.index.status").toBoolean()

    /** 是否启用更宽松的优化策略。这适用于多数需要加入到缓存中的集合，进行更详细的忽略检查。 */
    fun relaxOptimize() = System.getProperty("pls.relax.optimize").toBoolean()

    /** 是否在打开项目后，刷新内置规则目录（仅限一次）。 */
    fun refreshBuiltIn() = System.getProperty("pls.refresh.builtIn").toBoolean()
}
