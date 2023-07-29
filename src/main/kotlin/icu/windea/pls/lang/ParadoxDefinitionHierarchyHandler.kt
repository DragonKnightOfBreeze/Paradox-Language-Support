package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*

object ParadoxDefinitionHierarchyHandler {
    fun <T: ParadoxDefinitionHierarchyIndex<V>, V> processQuery(
        index: T,
        project: Project,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: Map<String, List<V>>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if(SearchScope.isEmptyScope(scope)) return true
        
        return doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = index.getFileData(file, project)
            processor(file, fileData)
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}
