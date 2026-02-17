package icu.windea.pls.lang

import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.util.FilePathBasedModificationTracker
import icu.windea.pls.core.util.MergedModificationTracker
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 用于追踪更改 - 具有更高的精确度，提高缓存命中率。
 */
object ParadoxModificationTrackers {
    /** 追踪任意游戏或模组目录中的脚本文件的更改。 */
    val ScriptFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的本地化文件的更改。 */
    val LocalisationFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的 CSV 文件的更改。 */
    val CsvFile = SimpleModificationTracker()

    val ScriptFileMap = ConcurrentHashMap<String, FilePathBasedModificationTracker>()

    val ScriptedVariables = scriptFileFromPatterns("common/scripted_variables/**/*.txt")
    val InlineScripts = scriptFileFromPatterns("common/inline_scripts/**/*.txt")

    val PreferredLocale = SimpleModificationTracker()
    val FilePath = SimpleModificationTracker()
    val ParameterConfigInference = SimpleModificationTracker()
    val InlineScriptConfigInference = SimpleModificationTracker()
    val DefinitionScopeContextInference = SimpleModificationTracker()

    val Resolve = MergedModificationTracker(
        ScriptFile,
        LocalisationFile,
        PreferredLocale,
        FilePath,
        ParameterConfigInference,
        InlineScriptConfigInference,
    )
    val Scope = DefinitionScopeContextInference

    fun scriptFileFromPatterns(vararg patterns: String): FilePathBasedModificationTracker {
        if (patterns.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        return scriptFileFrom(patterns.toSortedSet())
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, vararg definitionTypes: String): FilePathBasedModificationTracker {
        if (definitionTypes.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        val configs = definitionTypes.mapNotNull { configGroup.types[it] }
        return scriptFileFromConfigs(configs)
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, definitionTypes: Collection<String>): FilePathBasedModificationTracker {
        if (definitionTypes.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        val configs = definitionTypes.mapNotNull { configGroup.types[it] }
        return scriptFileFromConfigs(configs)
    }

    private fun scriptFileFrom(patterns: SortedSet<String>): FilePathBasedModificationTracker {
        if (patterns.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        val key = patterns.joinToString(";")
        return ScriptFileMap.getOrPut(key) { FilePathBasedModificationTracker(patterns) }
    }

    private fun scriptFileFromConfigs(configs: List<CwtTypeConfig>): FilePathBasedModificationTracker {
        if (configs.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
        if (patterns.isEmpty()) return FilePathBasedModificationTracker.NEVER_CHANGED
        return scriptFileFrom(patterns)
    }
}
