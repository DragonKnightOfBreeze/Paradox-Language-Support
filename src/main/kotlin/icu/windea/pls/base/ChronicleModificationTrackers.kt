package icu.windea.pls.base

import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.util.MergedModificationTracker
import icu.windea.pls.core.util.PatternsBasedModificationTracker
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object ChronicleModificationTrackers {
    // 3.0.2+ may be better to move to `icu.windea.pls.base.analysis`?

    val PreferredLocale = SimpleModificationTracker()
    val FilePath = SimpleModificationTracker()
    val ParameterConfigInference = SimpleModificationTracker()
    val InlineScriptConfigInference = SimpleModificationTracker()
    val DefinitionScopeContextInference = SimpleModificationTracker()

    /** 追踪任意游戏或模组目录中的脚本文件的更改。 */
    val ScriptFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的本地化文件的更改。 */
    val LocalisationFile = SimpleModificationTracker()
    /** 追踪任意游戏或模组目录中的 CSV 文件的更改。 */
    val CsvFile = SimpleModificationTracker()

    val ScriptFileMap = ConcurrentHashMap<String, PatternsBasedModificationTracker>()

    val ScriptedVariables = scriptFileFromFilePathPatterns("common/scripted_variables/**/*.txt")
    val InlineScripts = scriptFileFromFilePathPatterns("common/inline_scripts/**/*.txt")

    val ConfigResolution = MergedModificationTracker(
        ScriptFile,
        LocalisationFile,
        PreferredLocale,
        FilePath,
        ParameterConfigInference,
        InlineScriptConfigInference,
    )
    val ScopeResolution = DefinitionScopeContextInference

    val ScriptExpressionResolution = ConfigResolution
    val LocalisationExpressionResolution = MergedModificationTracker(
        ScriptFile,
        LocalisationFile,
        PreferredLocale,
    )
    val CsvExpressionResolution = MergedModificationTracker(
        ScriptFile,
    )

    fun scriptFileFromFilePathPatterns(vararg filePathPatterns: String): PatternsBasedModificationTracker {
        return scriptFileFromFilePathPatterns(filePathPatterns.toList())
    }

    fun scriptFileFromFilePathPatterns(filePathPatterns: Collection<String>): PatternsBasedModificationTracker {
        if (filePathPatterns.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val patterns = filePathPatterns.toSortedSet()
        val key = patterns.joinToString(";")
        return ScriptFileMap.getOrPut(key) { PatternsBasedModificationTracker(patterns) }
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, vararg definitionTypes: String): PatternsBasedModificationTracker {
        return scriptFileFromDefinitionTypes(configGroup, definitionTypes.toList())
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, definitionTypes: Collection<String>): PatternsBasedModificationTracker {
        if (definitionTypes.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val configs = definitionTypes.mapNotNull { configGroup.types[it] }
        return scriptFileFromConfigs(configs)
    }

    fun scriptFileFromConfigs(vararg configs: CwtConfig<*>): PatternsBasedModificationTracker {
        return scriptFileFromConfigs(configs.toList())
    }

    fun scriptFileFromConfigs(configs: Collection<CwtConfig<*>>): PatternsBasedModificationTracker {
        if (configs.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val patterns = sortedSetOf<String>()
        configs.forEach { config ->
            if(config is CwtFilePathMatchableConfig) patterns += config.filePathPatterns
        }
        return scriptFileFromFilePathPatterns(patterns)
    }

    fun expression(element: ParadoxExpressionElement): ModificationTracker {
        return when (element) {
            is ParadoxScriptExpressionElement -> ScriptExpressionResolution
            is ParadoxLocalisationExpressionElement -> LocalisationExpressionResolution
            is ParadoxCsvExpressionElement -> CsvExpressionResolution
            else -> ModificationTracker.EVER_CHANGED
        }
    }
}
