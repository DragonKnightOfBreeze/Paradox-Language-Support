package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.*
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
        val targetGameType = selector.gameType ?: return
        
        val distinctInFile = selector.selectors.findIsInstance<ParadoxWithSearchScopeTypeSelector<*>>()
            ?.searchScopeType?.distinctInFile() ?: true
        
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(ParadoxFileManager.isLightFile(file)) return@p true
            val complexEnumValueGroup = ParadoxComplexEnumValueIndex.getData(enumName, file, project)
            if(complexEnumValueGroup.isNullOrEmpty()) return@p true
            val psiFile = file.toPsiFile<PsiFile>(project) ?: return@p true
            if(name == null) {
                for(infos in complexEnumValueGroup.values) {
                    for(info in infos) {
                        if(targetGameType == info.gameType) {
                            info.withFile(psiFile) { consumer.process(info) }
                        }
                        if(distinctInFile) break
                    }
                }
            } else {
                val infos = complexEnumValueGroup[name] ?: return@p true
                for(info in infos) {
                    if(targetGameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                    if(distinctInFile) break
                }
            }
            true
        }, scope)
    }
}