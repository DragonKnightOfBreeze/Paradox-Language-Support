package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*

/**
 * 内联脚本调用的查询器。
 */
class ParadoxInlineScriptSearcher : QueryExecutorBase<ParadoxInlineScriptInfo, ParadoxInlineScriptSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptSearch.SearchParameters, consumer: Processor<in ParadoxInlineScriptInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val expression = queryParameters.expression
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            FileBasedIndex.getInstance().processValues(ParadoxInlineScriptIndex.NAME, expression, null, p@{ file, value ->
                ProgressManager.checkCanceled()
                ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved
                if(selectGameType(file) != gameType) return@p true //check game type at file level
                val inlineScriptInfos = value
                if(inlineScriptInfos.isNullOrEmpty()) return@p true
                inlineScriptInfos.forEachFast { info ->
                    if(gameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                }
                true
            }, scope)
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        ProgressManager.checkCanceled()
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
    
    private inline fun <T> ParadoxInlineScriptInfo.withFile(file: PsiFile, action: () -> T): T {
        this.file = file
        val r = action()
        this.file = null
        return r
    }
}