package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*

@Service(Service.Level.APP)
class ParadoxModificationTrackerProvider {
    //其实可以区分游戏类型，但是一般情况下不需要这么做
    
    val ScriptFileTracker = SimpleModificationTracker()
    
    val ScriptFileTrackers = mutableMapOf<String, PathModificationTracker>().synced()
    
    /**
     * 这里传入的扩展名应当是小写的且不包含"."。
     * @param keyString 例子：`path`, `path:txt`, `path1:txt|path2:txt,yml`
     */
    fun ScriptFileTracker(keyString: String): PathModificationTracker {
        return ScriptFileTrackers.getOrPut(keyString) { PathModificationTracker(keyString) }
    }
    
    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables:txt")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts:txt")
    
    val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
    
    companion object {
        @JvmStatic
        fun getInstance() = service<ParadoxModificationTrackerProvider>()
        
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxProjectModificationTrackerProvider>()
    }
}

@Service(Service.Level.PROJECT)
class ParadoxProjectModificationTrackerProvider(
    val project: Project
) {
    val ScriptFileTracker = PsiModificationTracker.getInstance(project).forLanguage(ParadoxScriptLanguage)
}


class PathModificationTracker(keyString: String) : SimpleModificationTracker() {
    val keys = keyString.split('|').map { 
        val i = it.indexOf(':')
        if(i == -1) PathModificationTrackerKey(it, emptySet())
        else PathModificationTrackerKey(it.substring(0, i), it.substring(i + 1).split(',').toSortedSet()) 
    }
}

class PathModificationTrackerKey(val path: String, val extensions: Set<String>)