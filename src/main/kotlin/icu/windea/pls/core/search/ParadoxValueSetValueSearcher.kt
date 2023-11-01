package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*

/**
 * 值集值的查询器。
 */
class ParadoxValueSetValueSearcher : QueryExecutorBase<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxValueSetValueSearch.SearchParameters, consumer: Processor<in ParadoxValueSetValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        
        val name = queryParameters.name
        val valueSetNames = queryParameters.valueSetNames
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return
        
        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxExpressionIndexInstance.getFileData(file, project, ParadoxExpressionIndexId.ValueSetValue)
            if(fileData.isEmpty()) return@p true
            fileData.forEachFast f@{ info ->
                if(info.valueSetName !in valueSetNames) return@f
                if(name != null && name != info.name) return@f
                val r = info.withVirtualFile(file) { consumer.process(info) }
                if(!r) return@p false
            }
            
            true
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope).let { if(!it) return false }
        FileTypeIndex.processFiles(ParadoxLocalisationFileType, processor, scope).let { if(!it) return false }
        return true
    }
}

