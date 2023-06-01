package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.hierarchy.impl.*
import icu.windea.pls.lang.model.*

object ParadoxDefinitionHierarchyHandler {
    fun processQuery(
        supportId: String,
        scope: GlobalSearchScope,
        processor: FileBasedIndex.ValueProcessor<List<ParadoxDefinitionHierarchyInfo>>
    ): Boolean {
        ProgressManager.checkCanceled()
        if(SearchScope.isEmptyScope(scope)) return true
        
        return FileBasedIndex.getInstance().processValues(ParadoxDefinitionHierarchyIndex.NAME, supportId, null, processor, scope)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processEventsInOnAction(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxEventInOnActionHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInOnActionDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        ParadoxEventInOnActionHierarchyContext.processor(file, value)
    }
}

object ParadoxEventInOnActionHierarchyContext

inline fun ParadoxDefinitionHierarchyHandler.processEventsInEffect(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxEventInEffectHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInEffectDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        ParadoxEventInEffectHierarchyContext.processor(file, value)
    }
}

object ParadoxEventInEffectHierarchyContext {
    val ParadoxDefinitionHierarchyInfo.containingEventScope: String? by ParadoxEventInEffectDefinitionHierarchySupport.containingEventScopeKey
    val ParadoxDefinitionHierarchyInfo.scopesElementOffset: Int? by ParadoxEventInEffectDefinitionHierarchySupport.scopesElementOffsetKey
}

inline fun ParadoxDefinitionHierarchyHandler.processOnActionsInEffect(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxEventInEffectHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxOnActionInEffectDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        ParadoxEventInEffectHierarchyContext.processor(file, value)
    }
}

object ParadoxOnActionsInEffectHierarchyContext {
    val ParadoxDefinitionHierarchyInfo.containingEventScope: String? by ParadoxOnActionInEffectDefinitionHierarchySupport.containingEventScopeKey
    val ParadoxDefinitionHierarchyInfo.scopesElementOffset: Int? by ParadoxOnActionInEffectDefinitionHierarchySupport.scopesElementOffsetKey
}

inline fun ParadoxDefinitionHierarchyHandler.processLocalisationParameters(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxLocalisationParameterHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxLocalisationParameterDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        ParadoxLocalisationParameterHierarchyContext.processor(file, value)
    }
}

object ParadoxLocalisationParameterHierarchyContext {
    val ParadoxDefinitionHierarchyInfo.localisationName: String? by ParadoxLocalisationParameterDefinitionHierarchySupport.localisationNameKey
}

inline fun ParadoxDefinitionHierarchyHandler.processInferredScopeContextAwareDefinitions(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: ParadoxLocalisationParameterHierarchyContext.(file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxInferredScopeContextAwareDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        ParadoxLocalisationParameterHierarchyContext.processor(file, value)
    }
}
