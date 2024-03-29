package icu.windea.pls.lang

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import java.util.concurrent.*

/**
 * 用于追踪PSI更改 - 具有更高的精确度，提高缓存命中率。
 */
@Service(Service.Level.PROJECT)
class ParadoxModificationTrackerProvider(project: Project) {
    val ScriptFileTracker = CompositeModificationTracker(PsiModificationTracker.getInstance(project).forLanguage(ParadoxScriptLanguage))
    val LocalisationFileTracker = CompositeModificationTracker(PsiModificationTracker.getInstance(project).forLanguage(ParadoxLocalisationLanguage))
    
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
    
    companion object {
        @JvmField val ParameterConfigInferenceTracker = SimpleModificationTracker()
        @JvmField val InlineScriptConfigInferenceTracker = SimpleModificationTracker()
        @JvmField val DefinitionScopeContextInferenceTracker = SimpleModificationTracker()
        
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxModificationTrackerProvider>()
    }
}