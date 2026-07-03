package icu.windea.pls.lang

import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.util.MergedModificationTracker
import icu.windea.pls.core.util.PatternsBasedModificationTracker
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import java.util.concurrent.ConcurrentHashMap

object ParadoxModificationTrackers {
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
        if (filePathPatterns.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val patterns = filePathPatterns.toSortedSet()
        val key = patterns.joinToString(";")
        return ScriptFileMap.getOrPut(key) { PatternsBasedModificationTracker(patterns) }
    }

    fun scriptFileFromFilePathPatterns(filePathPatterns: Collection<String>): PatternsBasedModificationTracker {
        if (filePathPatterns.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val patterns = filePathPatterns.toSortedSet()
        val key = patterns.joinToString(";")
        return ScriptFileMap.getOrPut(key) { PatternsBasedModificationTracker(patterns) }
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, vararg definitionTypes: String): PatternsBasedModificationTracker {
        if (definitionTypes.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val configs = definitionTypes.mapNotNull { configGroup.types[it] }
        return scriptFileFromConfigs(configs)
    }

    fun scriptFileFromDefinitionTypes(configGroup: CwtConfigGroup, definitionTypes: Collection<String>): PatternsBasedModificationTracker {
        if (definitionTypes.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val configs = definitionTypes.mapNotNull { configGroup.types[it] }
        return scriptFileFromConfigs(configs)
    }

    private fun scriptFileFromConfigs(configs: List<CwtTypeConfig>): PatternsBasedModificationTracker {
        if (configs.isEmpty()) return PatternsBasedModificationTracker.NEVER_CHANGED
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
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
