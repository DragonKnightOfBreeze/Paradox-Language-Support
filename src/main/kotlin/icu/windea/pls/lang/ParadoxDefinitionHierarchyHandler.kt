package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*

object ParadoxDefinitionHierarchyHandler {
    fun processQuery(selector: ChainedParadoxSelector<ParadoxDefinitionHierarchyInfo>, processor: (file: VirtualFile, fileData: Map<String, List<ParadoxDefinitionHierarchyInfo>>) -> Boolean) {
        ProgressManager.checkCanceled()
        val scope = selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val project = selector.project
        val gameType = selector.gameType
        
        doProcessFiles(selector.scope) p@{file ->
            ProgressManager.checkCanceled()
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val data = ParadoxDefinitionHierarchyIndex.getData(file, project)
            processor(file, data)
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        ProgressManager.checkCanceled()
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}