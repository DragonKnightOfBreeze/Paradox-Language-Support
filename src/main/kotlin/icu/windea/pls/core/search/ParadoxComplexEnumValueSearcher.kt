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
 * 复杂枚举的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxComplexEnumValueInfo, ParadoxComplexEnumValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, consumer: Processor<in ParadoxComplexEnumValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        
        val name = queryParameters.name
        val enumName = queryParameters.enumName
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType
        
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(file.fileInfo == null) return@p true
            if(ParadoxFileManager.isLightFile(file)) return@p true
            val complexEnumValues = ParadoxComplexEnumValueIndex.getData(enumName, file, project)
            if(complexEnumValues.isNullOrEmpty()) return@p true
            val psiFile = file.toPsiFile<PsiFile>(project) ?: return@p true
            if(name == null) {
                for(info in complexEnumValues.values) {
                    if(gameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                }
            } else {
                val info = complexEnumValues[name] ?: return@p true
                if(gameType == info.gameType) {
                    info.withFile(psiFile) { consumer.process(info) }
                }
            }
            true
        }, scope)
    }
}