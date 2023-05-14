package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*

@Service(Service.Level.APP)
class ParadoxModificationTrackerProvider {
    //其实可以区分游戏类型，但是一般情况下不需要这么做
    
    val ScriptFileTracker = SimpleModificationTracker()
    
    val ScriptFileTrackers = mutableMapOf<String, SimpleModificationTracker>().synced()
    
    fun ScriptFileTracker(path: String): SimpleModificationTracker {
        return ScriptFileTrackers.getOrPut(path) { SimpleModificationTracker() }
    }
    
    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts")
    
    //val ParameterValueConfigInferenceTracker get() = ScriptFileTracker //目前没有想到更好的追踪方式
    val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
    
    companion object {
        @JvmStatic
        fun getInstance() = service<ParadoxModificationTrackerProvider>()
    }
}