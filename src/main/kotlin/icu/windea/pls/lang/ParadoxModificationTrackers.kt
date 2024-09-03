package icu.windea.pls.lang

import com.intellij.openapi.util.*
import icu.windea.pls.core.util.*
import java.util.concurrent.*

/**
 * 用于追踪更改 - 具有更高的精确度，提高缓存命中率。
 */
object ParadoxModificationTrackers {
    val ScriptFileTracker = SimpleModificationTracker()
    val LocalisationFileTracker = SimpleModificationTracker()
    
    val ScriptFileTrackers = ConcurrentHashMap<String, FilePathBasedModificationTracker>()
    
    fun ScriptFileTracker(key: String): FilePathBasedModificationTracker {
        return ScriptFileTrackers.getOrPut(key) { FilePathBasedModificationTracker(key) }
    }
    
    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables/**/*.txt")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts/**/*.txt")
    
    val ParameterConfigInferenceTracker = SimpleModificationTracker()
    val InlineScriptConfigInferenceTracker = SimpleModificationTracker()
    val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
}
