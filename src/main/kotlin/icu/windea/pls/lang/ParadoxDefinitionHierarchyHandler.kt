package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
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

inline  fun ParadoxDefinitionHierarchyHandler.processEventsInOnAction(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInOnActionDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        processor(file, value)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processEventsInEvent(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxEventInEventDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        processor(file, value)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processLocalisationParameters(
    gameType: ParadoxGameType,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    val supportId = ParadoxLocalisationParameterDefinitionHierarchySupport.ID
    return processQuery(supportId, scope) p@{ file, value ->
        if(selectGameType(file) != gameType) return@p true //check game type at file level
        processor(file, value)
    }
}