package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.lang.model.*
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
            //ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
            val psiFile = file.toPsiFile(project) ?: return@p true //ensure file info is resolved here
            if(selectGameType(file) != gameType) return@p true //check game type at file level
            
            val fileData = ParadoxInlineScriptUsageIndex.getInstance().getFileData(file, project)
            if(fileData.isEmpty()) return@p true
            val inlineScriptUsageInfos = fileData[expression]
            if(inlineScriptUsageInfos.isNullOrEmpty()) return@p true
            inlineScriptUsageInfos.forEachFast { info ->
                info.withFile(psiFile) { consumer.process(info) }
            }
            true
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
    
    private inline fun <T> ParadoxInlineScriptUsageInfo.withFile(file: PsiFile, action: () -> T): T {
        this.file = file
        val r = action()
        this.file = null
        return r
    }
}