package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.hierarchy.impl.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*

object ParadoxDefinitionHierarchyHandler {
    fun processQuery(
        gameType: ParadoxGameType,
        project: Project,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: Map<String, List<ParadoxDefinitionHierarchyInfo>>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if(SearchScope.isEmptyScope(scope)) return true
        
        ProgressManager.checkCanceled()
        return FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val data = ParadoxDefinitionHierarchyIndex.getData(file, project)
            processor(file, data)
        }, scope)
    }
    
}

inline  fun ParadoxDefinitionHierarchyHandler.processEventsInOnAction(
    gameType: ParadoxGameType,
    project: Project,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    return processQuery(gameType, project, scope) p@{ file, fileData ->
        val infos = fileData.get(ParadoxEventInOnActionDefinitionHierarchySupport.ID) ?: return@p true
        processor(file, infos)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processEventsInEvent(
    gameType: ParadoxGameType,
    project: Project,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    return processQuery(gameType, project, scope) p@{ file, fileData ->
        val infos = fileData.get(ParadoxEventInEventDefinitionHierarchySupport.ID) ?: return@p true
        processor(file, infos)
    }
}

inline fun ParadoxDefinitionHierarchyHandler.processLocalisationParameters(
    gameType: ParadoxGameType,
    project: Project,
    scope: GlobalSearchScope,
    crossinline processor: (file: VirtualFile, infos: List<ParadoxDefinitionHierarchyInfo>) -> Boolean
): Boolean {
    return processQuery(gameType, project, scope) p@{ file, fileData ->
        val infos = fileData.get(ParadoxLocalisationParameterDefinitionHierarchySupport.ID) ?: return@p true
        processor(file, infos)
    }
}