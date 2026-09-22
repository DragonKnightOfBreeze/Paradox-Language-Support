package icu.windea.pls.base

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import icu.windea.pls.core.isClassPresent

object ChronicleCapacities {
    /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
    fun includeSqlite(): Boolean = "org.sqlite.JDBC".isClassPresent()

    /** 是否记录缓存的统计数据。 */
    fun recordCacheStats(): Boolean = model.recordCacheStats

    /** 是否记录索引的统计数据。 */
    fun recordIndexStats(): Boolean = model.recordIndexStats

    /** 是否在打开项目后，刷新内置规则文件（仅限一次）。 */
    fun refreshBuiltInConfigDirectories(): Boolean = model.refreshBuiltInConfigDirectories

    /** 处理规则数据时，是否保留文件规则列表到其用户数据中（默认不保留）。 */
    fun keepFileConfigs(): Boolean = model.keepFileConfigs

    /** 处理成员规则的选项元数据时，是否保留选项规则列表到其用户数据中（默认仅为内部规则保留）。 */
    fun keepOptionConfigs(): Boolean = model.keepOptionConfigs

    /** 收集得到的匹配候选项的最大数量，如果超出则会改为使用回退匹配。默认为 64。用于优化性能。 */
    fun maxMatchCandidateSize(): Int = model.maxMatchCandidateSize

    /** 定义相对于脚本文件的最大深度，如果超出则会被忽略。从 0 开始，默认为 4。用于优化性能。 */
    fun maxDefinitionDepth(): Int = model.maxDefinitionDepth

    // region Implementations

    @Volatile private var model = Model()

    private class Model {
        val recordCacheStats = System.getProperty("chronicle.capacities.recordCacheStats").toBoolean()
        val recordIndexStats = System.getProperty("chronicle.capacities.recordIndexStats").toBoolean()
        val refreshBuiltInConfigDirectories = System.getProperty("chronicle.capacities.refreshBuiltInConfigDirectories").toBoolean()
        val keepFileConfigs = System.getProperty("chronicle.capacities.keepFileConfigs").toBoolean()
        val keepOptionConfigs = System.getProperty("chronicle.capacities.keepOptionConfigs").toBoolean()
        val maxMatchCandidateSize = System.getProperty("chronicle.capacities.maxMatchCandidateSize")?.toIntOrNull() ?: 64
        val maxDefinitionDepth = System.getProperty("chronicle.capacities.maxDefinitionDepth")?.toIntOrNull() ?: 4
    }

    internal class Listener : ProjectActivity {
        override suspend fun execute(project: Project) {
            // reinitialize on project start
            model = Model()
        }
    }

    // endregion
}
