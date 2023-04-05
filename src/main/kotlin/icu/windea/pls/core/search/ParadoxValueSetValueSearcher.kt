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
 * 值集值值的查询器。
 */
class ParadoxValueSetValueSearcher : QueryExecutorBase<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxValueSetValueSearch.SearchParameters, consumer: Processor<in ParadoxValueSetValueInfo>) {
        val name = queryParameters.name
        val valueSetName = queryParameters.valueSetName
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType
        val scope = queryParameters.selector.scope
        
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(file.fileInfo == null) return@p true
            if(ParadoxFileManager.isLightFile(file)) return@p true
            val psiFile = file.toPsiFile<PsiFile>(project) ?: return@p true
            val valueSetValues = ParadoxValueSetValueIndex.getData(valueSetName, psiFile)
            if(valueSetValues.isNullOrEmpty()) return@p true
            if(name == null) {
                for(info in valueSetValues.values) {
                    if(gameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                }
            } else {
                val info = valueSetValues[name] ?: return@p true
                if(gameType == info.gameType) {
                    info.withFile(psiFile) { consumer.process(info) }
                }
            }
            true
        }, scope)
    }
}

