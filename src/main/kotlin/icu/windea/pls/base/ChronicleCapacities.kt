package icu.windea.pls.base

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import icu.windea.pls.core.isClassPresent

object ChronicleCapacities {
    /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
    fun includeSqlite(): Boolean = "org.sqlite.JDBC".isClassPresent()

    /** 是否记录缓存的统计数据。 */
    fun recordCacheStats(): Boolean = System.getProperty("chronicle.capacities.recordCacheStats").toBoolean()

    /** 是否记录索引的统计数据。 */
    fun recordIndexStats(): Boolean = System.getProperty("chronicle.capacities.recordIndexStats").toBoolean()

    /** 是否在打开项目后，刷新内置规则文件（仅限一次）。 */
    fun refreshBuiltInConfigDirectories(): Boolean = System.getProperty("chronicle.capacities.refreshBuiltInConfigDirectories").toBoolean()

    /** 处理规则数据时，是否保留文件规则列表到其用户数据中（默认不保留）。 */
    fun keepFileConfigs(): Boolean = _keepFileConfigs

    /** 处理成员规则的选项元数据时，是否保留选项规则列表到其用户数据中（默认仅为内部规则保留）。 */
    fun keepOptionConfigs(): Boolean = _keepOptionConfigs

    /** 定义相对于脚本文件的最大深度，从0开始（默认为4）。用于优化性能。 */
    fun maxDefinitionDepth(): Int = _maxDefinitionDepth

    // region Implementations

    // 3.0.1 cache to optimize (very few) performance
    @Volatile private var _keepFileConfigs: Boolean = computeKeepFileConfigs()
    @Volatile private var _keepOptionConfigs: Boolean = computeKeepOptionConfigs()
    @Volatile private var _maxDefinitionDepth: Int = computeMaxDefinitionDepth()

    private fun computeKeepFileConfigs() = System.getProperty("chronicle.capacities.keepFileConfigs").toBoolean()
    private fun computeKeepOptionConfigs() = System.getProperty("chronicle.capacities.keepFileConfigs").toBoolean()
    private fun computeMaxDefinitionDepth() = System.getProperty("chronicle.capacities.keepFileConfigs")?.toIntOrNull() ?: 4

    class Listener : ProjectActivity {
        override suspend fun execute(project: Project) {
            // reinitialize language capacities
            _keepFileConfigs = computeKeepFileConfigs()
            _keepOptionConfigs = computeKeepOptionConfigs()
            _maxDefinitionDepth = computeMaxDefinitionDepth()
        }
    }

    // endregion
}
