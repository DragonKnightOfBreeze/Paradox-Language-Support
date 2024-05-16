package icu.windea.pls.lang

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.util.*
import java.util.concurrent.*

/**
 * 用于追踪更改 - 具有更高的精确度，提高缓存命中率。
 */
object ParadoxModificationTrackers {
    val ScriptFileTracker = SimpleModificationTracker()
    val LocalisationFileTracker = SimpleModificationTracker()
    
    val ScriptFileTrackers = ConcurrentHashMap<String, PathBasedModificationTracker>()
    
    fun ScriptFileTracker(pattern: String): PathBasedModificationTracker {
        return ScriptFileTrackers.getOrPut(pattern) { PathBasedModificationTracker(pattern) }
    }
    
    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables/**/*.txt")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts/**/*.txt")
    
    val ParameterConfigInferenceTracker = SimpleModificationTracker()
    val InlineScriptConfigInferenceTracker = SimpleModificationTracker()
    val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
    
    fun getPatternFromTypeConfigs(configs: Collection<CwtTypeConfig>): String {
        val paths = mutableSetOf<String>()
        configs.forEach { config ->
            config.path?.let { p -> paths += p }
        }
        return paths.joinToString(";")
    }
    
    fun getPatternFromComplexEnumConfigs(configs: Collection<CwtComplexEnumConfig>): String {
        val paths = mutableSetOf<String>()
        configs.forEach { config ->
            config.path.forEach { p -> paths += p }
        }
        return paths.joinToString(";")
    }
}