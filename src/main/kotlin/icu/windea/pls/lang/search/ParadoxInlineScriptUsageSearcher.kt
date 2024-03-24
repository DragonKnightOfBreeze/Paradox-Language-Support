package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*

/**
 * 内联脚本使用的查询器。
 */
class ParadoxInlineScriptUsageSearcher : QueryExecutorBase<ParadoxInlineScriptUsageInfo, ParadoxInlineScriptUsageSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptUsageSearch.SearchParameters, consumer: Processor<in ParadoxInlineScriptUsageInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val expression = queryParameters.expression
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType
        
        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxExpressionIndexInstance.getFileData(file, project, ParadoxExpressionIndexId.InlineScriptUsage)
            if(fileData.isEmpty()) return@p true
            fileData.forEachFast f@{ info ->
                if(expression != info.expression) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if(!r) return@p false
            }
            
            true
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}