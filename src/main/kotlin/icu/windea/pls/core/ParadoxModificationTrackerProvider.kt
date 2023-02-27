package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*

@Service(Service.Level.APP)
class ParadoxModificationTrackerProvider {
    //其实可以区分游戏类型，但是一般情况下不需要这么做
    
    val ScriptFile = SimpleModificationTracker()
    val OnAction = SimpleModificationTracker()
    val InlineScript = SimpleModificationTracker()
    val Modifier = SimpleModificationTracker()
    
    val DefinitionScopeContextInference = SimpleModificationTracker()
    val LocalisationCommandScopeContextInference = SimpleModificationTracker()
    
    companion object {
        @JvmStatic
        fun getInstance() = service<ParadoxModificationTrackerProvider>()
    }
}