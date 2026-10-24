package icu.windea.pls

import icu.windea.pls.core.isClassPresent
import icu.windea.pls.core.orNull

/**
 * 可在运行时调整的所有能力项。
 */
object ChronicleCapabilities {
    /**
     * 通用的能力项。涉及全局行为、基础设施行为、语义行为等多个层面。
     */
    object General {
        /**
         * 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。
         */
        var includeSqlite: Boolean = "org.sqlite.JDBC".isClassPresent()

        /**
         * 是否记录缓存的统计数据。
         *
         * 对应系统属性：`chronicle.recordCacheStats`。
         */
        var recordCacheStats: Boolean = getBooleanProperty("chronicle.recordCacheStats", false)
        /**
         * 是否记录索引的统计数据。
         *
         * 对应系统属性：`chronicle.recordIndexStats`。
         */
        var recordIndexStats: Boolean = getBooleanProperty("chronicle.recordIndexStats", false)

        /**
         * 是否在打开项目后，刷新内置规则文件（仅限一次）。
         *
         * 对应系统属性：`chronicle.refreshBuiltInConfigDirectories`。
         */
        var refreshBuiltInConfigDirectories: Boolean = getBooleanProperty("chronicle.refreshBuiltInConfigDirectories", false)
        /**
         * 处理规则数据时，是否保留文件规则列表到其用户数据中。默认不保留。
         *
         * 对应系统属性：`chronicle.keepFileConfigs`。
         */
        var keepFileConfigs: Boolean = getBooleanProperty("chronicle.keepFileConfigs", false)
        /**
         * 处理成员规则的选项元数据时，是否保留选项规则列表到其用户数据中。默认仅为内部规则保留。
         *
         * 对应系统属性：`chronicle.keepOptionConfigs`。
         */
        var keepOptionConfigs: Boolean = getBooleanProperty("chronicle.keepOptionConfigs", false)

        /**
         * 定义相对于脚本文件的最大深度，如果超出则会被忽略。从 0 开始，默认为 4。用于优化性能。
         *
         * 对应系统属性：`chronicle.maxDefinitionDepth`。
         */
        var maxDefinitionDepth: Int = getIntProperty("chronicle.maxDefinitionDepth", 4, 0..40)
        /**
         * 收集得到的匹配候选项的最大数量，如果超出则会改为使用回退匹配。默认为 64。用于优化性能。
         *
         * 对应系统属性：`chronicle.maxMatchCandidateSize`。
         */
        var maxMatchCandidateSize: Int = getIntProperty("chronicle.maxMatchCandidateSize", 64, 0..640)
        /**
         * 收集得到的处理后的匹配候选项的最大数量，如果超出则会改为使用回退匹配。默认为 16。用于优化性能。
         *
         * 对应系统属性：`chronicle.maxProcessedMatchCandidateSize`。
         */
        var maxProcessedMatchCandidateSize: Int = getIntProperty("chronicle.maxProcessedMatchCandidateSize", 16, 0..160)
    }

    /**
     * 缓存相关的能力项。
     */
    object Cache {
        var arguments: Boolean = getBooleanProperty("chronicle.cache.arguments", true)
        var childOccurrences: Boolean = getBooleanProperty("chronicle.cache.childOccurrences", true)
        var complexEnumValue: Boolean = getBooleanProperty("chronicle.cache.complexEnumValue", true)
        var configContext: Boolean = getBooleanProperty("chronicle.cache.configContext", true)
        var configs: Boolean = getBooleanProperty("chronicle.cache.configs", true)
        var configSymbol: Boolean = getBooleanProperty("chronicle.cache.configSymbol", true)
        var define: Boolean = getBooleanProperty("chronicle.cache.define", true)
        var definition: Boolean = getBooleanProperty("chronicle.cache.definition", true)
        var definitionInjection: Boolean = getBooleanProperty("chronicle.cache.definitionInjection", true)
        var presentableItems: Boolean = getBooleanProperty("chronicle.cache.presentableItems", true)
        var relatedItems: Boolean = getBooleanProperty("chronicle.cache.relatedItems", true)
        var rowConfig: Boolean = getBooleanProperty("chronicle.cache.rowConfig", true)
        var scopeContext: Boolean = getBooleanProperty("chronicle.cache.scopeContext", true)
        var textColor: Boolean = getBooleanProperty("chronicle.cache.textColor", true)
    }

    /**
     * 测试相关的能力项。
     */
    object Test {
        var includeAll = getBooleanProperty("chronicle.test.include.all", false)
        var includeAi = getBooleanProperty("chronicle.test.include.ai", false)
        var includeRemote = getBooleanProperty("chronicle.test.include.remote", false)
        var includeLocalEnv = getBooleanProperty("chronicle.test.include.local.env", false)
        var includeConfigGenerator = getBooleanProperty("chronicle.test.include.config.generator", false)
    }

    /**
     * 基准测试相关的能力项。
     *
     * 这些能力项通常通过 `-D...` 系统属性调整，并由 `ChronicleBenchmarkLauncher` 在 JMH 启动阶段读取。
     */
    object Benchmark {
        /**
         * 基准测试类所在目录。多个目录使用系统路径分隔符分隔。
         *
         * 对应系统属性：`chronicle.benchmark.classesDir`。
         * 未指定时，启动器会扫描 classpath。
         */
        var classesDir: String = getStringProperty("chronicle.benchmark.classesDir", "")

        /**
         * 基准测试报告的输出目录。
         *
         * 对应系统属性：`chronicle.benchmark.reportDir`。
         * 未指定时，启动器默认使用 `build/reports/benchmarks`。
         */
        var reportDir: String = getStringProperty("chronicle.benchmark.reportDir", "build/reports/benchmarks")

        /**
         * 要启用的 JMH profiler 列表。
         *
         * 对应系统属性：`chronicle.benchmark.profilers`，使用逗号分隔，例如 `gc`。
         * 未指定时不启用 profiler。
         */
        var profilers: String = getStringProperty("chronicle.benchmark.profilers", "")

        /**
         * 要应用的 JMH 参数过滤列表。
         *
         * 对应系统属性：`chronicle.benchmark.params`，使用逗号分隔的 `key=value` 形式，例如 `size=1000000`。无效项会被忽略。
         */
        var params: String = getStringProperty("chronicle.benchmark.params", "")
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

    private fun getStringProperty(key: String, defaultValue: String): String {
        val value = System.getProperty(key)?.trim()?.orNull() ?: defaultValue
        return value
    }

    // endregion
}
