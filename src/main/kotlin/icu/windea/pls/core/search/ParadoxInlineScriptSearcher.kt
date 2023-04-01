package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.tool.*

/**
 * 内联脚本调用的查询器。
 */
class ParadoxInlineScriptSearcher : QueryExecutorBase<ParadoxInlineScriptInfo, ParadoxInlineScriptSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptSearch.SearchParameters, consumer: Processor<in ParadoxInlineScriptInfo>) {
        val expression = queryParameters.expression
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType
        val scope = queryParameters.selector.scope
        
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(file.fileInfo == null) return@p true
            if(ParadoxFileManager.isLightFile(file)) return@p true
            val inlineScripts = ParadoxInlineScriptIndex.getData(expression, file, project)
            if(inlineScripts.isNullOrEmpty()) return@p true
            val psiFile = file.toPsiFile<PsiFile>(project) ?: return@p true
            for(info in inlineScripts) {
                ProgressManager.checkCanceled()
                if(gameType == info.gameType) {
                    info.withFile(psiFile) { consumer.process(info) }
                }
            }
            true
        }, scope)
    }
}