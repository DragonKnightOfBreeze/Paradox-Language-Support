package icu.windea.pls.core.psi

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.impl.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

//com.intellij.psi.util.PsiModificationTracker
//com.intellij.psi.impl.PsiModificationTrackerImpl

/**
 * 用于追踪PSI更改 - 具有更高的精确度，提高缓存命中率。
 */
class ParadoxPsiModificationTracker(project: Project) : PsiTreeChangePreprocessor {
    val ScriptFileTracker = PsiModificationTracker.getInstance(project).forLanguage(ParadoxScriptLanguage)
    
    val ScriptFileTrackers = ConcurrentHashMap<String, PathModificationTracker>()
    
    /**
     * 这里传入的扩展名应当是小写的且不包含"."。
     * @param keyString 例子：`path`, `path:txt`, `path1:txt|path2:txt,yml`
     */
    fun ScriptFileTracker(keyString: String): PathModificationTracker {
        return ScriptFileTrackers.getOrPut(keyString) { PathModificationTracker(keyString) }
    }
    
    val ScriptedVariablesTracker = ScriptFileTracker("common/scripted_variables:txt")
    val InlineScriptsTracker = ScriptFileTracker("common/inline_scripts:txt")
    
    //com.intellij.psi.impl.PsiModificationTrackerImpl.treeChanged
    
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if(!PsiModificationTrackerImpl.canAffectPsi(event)) return
        
        //这个方法应当尽可能地快
        
        val file = event.file ?: return
        if(file !is ParadoxScriptFile) return
        val fileInfo = file.fileInfo ?: return
        val filePath = fileInfo.pathToEntry.path
        val fileExtension = fileInfo.pathToEntry.fileExtension.lowercase() //ignore case
        //注意这里需要先获取服务再获取trackers
        val trackers = getInstance(file.project).ScriptFileTrackers.values
        for(tracker in trackers) {
            val keys = tracker.keys
            val keysSize = keys.size
            for(i in 0 until keysSize) {
                val key = keys[i]
                if((filePath.isEmpty() || key.path.matchesPath(filePath)) && (key.extensions.isEmpty() || key.extensions.contains(fileExtension))) {
                    tracker.incModificationCount()
                    break
                }
            }
        }
    }
    
    companion object {
        @JvmField val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
        
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxPsiModificationTracker>()
    }
}

class PathModificationTracker(keyString: String) : SimpleModificationTracker() {
    val keys = keyString.split('|').map {
        val i = it.indexOf(':')
        if(i == -1) PathModificationTrackerKey(it, emptySet())
        else PathModificationTrackerKey(it.substring(0, i), it.substring(i + 1).lowercase().split(',').toSortedSet())
    }
}

class PathModificationTrackerKey(val path: String, val extensions: Set<String>)