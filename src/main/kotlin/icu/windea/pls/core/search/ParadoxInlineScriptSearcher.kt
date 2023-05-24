package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.tool.*

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
            doProcessFiles(scope) p@{ file ->
                ProgressManager.checkCanceled()
                //NOTE 这里需要先获取psiFile，否则fileInfo可能未被解析
                val psiFile = file.toPsiFile(project) ?: return@p true
                if(file.fileInfo == null) return@p true
                if(ParadoxFileManager.isLightFile(file)) return@p true
                val inlineScriptInfos = ParadoxInlineScriptIndex.getData(expression, file, project)
                if(inlineScriptInfos.isNullOrEmpty()) return@p true
                inlineScriptInfos.forEachFast { info ->
                    if(gameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                }
                true
            }
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