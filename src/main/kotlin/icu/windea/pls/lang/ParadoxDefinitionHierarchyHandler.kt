package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.hierarchy.impl.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

object ParadoxDefinitionHierarchyHandler {
    fun processQuery(
        project: Project,
        supportId: String,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if(SearchScope.isEmptyScope(scope)) return true
        
        return doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxDefinitionHierarchyIndex.getInstance().getFileData(file, project)
            val infos = fileData.get(supportId)
            if(infos.isNullOrEmpty()) return@p true
            processor(file, infos)
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processEventsInOnAction(
    project: Project,
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInOnActionDefinitionHierarchySupport.ID
    return processQuery(project, supportId, gameType, scope) p@{ file, value ->
        processor(file, value)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processEventsInEffect(
    project: Project,
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxEventInEffectHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInEffectDefinitionHierarchySupport.ID
    return processQuery(project, supportId, gameType, scope) p@{ file, value ->
        ParadoxEventInEffectHierarchyContext.processor(file, value)
    }
}

object ParadoxEventInEffectHierarchyContext {
    val ParadoxDefinitionHierarchyInfo.containingEventScope: String? by ParadoxEventInEffectDefinitionHierarchySupport.containingEventScopeKey
    val ParadoxDefinitionHierarchyInfo.scopesElementOffset: Int? by ParadoxEventInEffectDefinitionHierarchySupport.scopesElementOffsetKey
}

inline fun ParadoxDefinitionHierarchyHandler.processOnActionsInEffect(
    project: Project,
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxOnActionInEffectHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxOnActionInEffectDefinitionHierarchySupport.ID
    return processQuery(project, supportId, gameType, scope) p@{ file, value ->
        ParadoxOnActionInEffectHierarchyContext.processor(file, value)
    }
}

object ParadoxOnActionInEffectHierarchyContext {
    val ParadoxDefinitionHierarchyInfo.containingEventScope: String? by ParadoxOnActionInEffectDefinitionHierarchySupport.containingEventScopeKey
    val ParadoxDefinitionHierarchyInfo.scopesElementOffset: Int? by ParadoxOnActionInEffectDefinitionHierarchySupport.scopesElementOffsetKey
}

inline fun ParadoxDefinitionHierarchyHandler.processInferredScopeContextAwareDefinitions(
    project: Project,
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxInferredScopeContextAwareDefinitionHierarchySupport.ID
    return processQuery(project, supportId, gameType, scope) p@{ file, value ->
        processor(file, value)
    }
}
