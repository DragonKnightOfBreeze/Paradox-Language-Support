package icu.windea.pls

import icu.windea.pls.core.isClassPresent

/**
 * 可在运行时调整的所有能力项。
 */
object ChronicleCapabilities {
    object General {
        /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
        var includeSqlite: Boolean = "org.sqlite.JDBC".isClassPresent()

        /** 是否记录缓存的统计数据。 */
        var recordCacheStats: Boolean = getBooleanProperty("chronicle.recordCacheStats", false)
        /** 是否记录索引的统计数据。 */
        var recordIndexStats: Boolean = getBooleanProperty("chronicle.recordIndexStats", false)

        /** 是否在打开项目后，刷新内置规则文件（仅限一次）。 */
        var refreshBuiltInConfigDirectories: Boolean = getBooleanProperty("chronicle.refreshBuiltInConfigDirectories", false)
        /** 处理规则数据时，是否保留文件规则列表到其用户数据中。默认不保留。 */
        var keepFileConfigs: Boolean = getBooleanProperty("chronicle.keepFileConfigs", false)
        /** 处理成员规则的选项元数据时，是否保留选项规则列表到其用户数据中。默认仅为内部规则保留。 */
        var keepOptionConfigs: Boolean = getBooleanProperty("chronicle.keepOptionConfigs", false)

        /** 定义相对于脚本文件的最大深度，如果超出则会被忽略。从 0 开始，默认为 4。用于优化性能。 */
        var maxDefinitionDepth: Int = getIntProperty("chronicle.maxDefinitionDepth", 4, 0..40)
        /** 收集得到的匹配候选项的最大数量，如果超出则会改为使用回退匹配。默认为 64。用于优化性能。 */
        var maxMatchCandidateSize: Int = getIntProperty("chronicle.maxMatchCandidateSize", 64, 0..640)
        /** 收集得到的处理后的匹配候选项的最大数量，如果超出则会改为使用回退匹配。默认为 16。用于优化性能。 */
        var maxProcessedMatchCandidateSize: Int = getIntProperty("chronicle.maxProcessedMatchCandidateSize", 16, 0..160)
    }

    object Cache {
        var configSymbolInfos: Boolean = getBooleanProperty("chronicle.cache.configSymbolInfos", true)
        var configContext: Boolean = getBooleanProperty("chronicle.cache.configContext", true)
        var configs: Boolean = getBooleanProperty("chronicle.cache.configs", true)
        var childOccurrences: Boolean = getBooleanProperty("chronicle.cache.childOccurrences", true)
        var rowConfig: Boolean = getBooleanProperty("chronicle.cache.rowConfig", true)
        var defineInfo: Boolean =getBooleanProperty("chronicle.cache.define", true)
        var complexEnumValueInfo: Boolean = getBooleanProperty("chronicle.cache.complexEnumValueInfo", true)
        var textColorInfo: Boolean = getBooleanProperty("chronicle.cache.textColor", true)
        var arguments: Boolean = getBooleanProperty("chronicle.cache.arguments", true)
    }

    object Test {
        var includeAll = getBooleanProperty("chronicle.test.include.all", false)
        var includeBenchmark = getBooleanProperty("chronicle.test.include.benchmark", false)
        var includeAi = getBooleanProperty("chronicle.test.include.ai", false)
        var includeRemote = getBooleanProperty("chronicle.test.include.remote", false)
        var includeLocalEnv = getBooleanProperty("chronicle.test.include.local.env", false)
        var includeConfigGenerator = getBooleanProperty("chronicle.test.include.config.generator", false)
    }

    // region Helpers

    private fun getBooleanProperty(key: String, defaultValue: Boolean): Boolean {
        val value = System.getProperty(key)?.toBoolean() ?: defaultValue
        return value
    }

    private fun getIntProperty(key: String, defaultValue: Int, range: ClosedRange<Int>? = null): Int {
        val value = System.getProperty(key)?.toIntOrNull() ?: defaultValue
        return if (range != null) value.coerceIn(range) else value
    }

    // endregion
}
