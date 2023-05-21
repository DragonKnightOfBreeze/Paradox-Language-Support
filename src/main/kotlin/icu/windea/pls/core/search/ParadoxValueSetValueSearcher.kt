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
 * 值集值值的查询器。
 */
class ParadoxValueSetValueSearcher : QueryExecutorBase<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxValueSetValueSearch.SearchParameters, consumer: Processor<in ParadoxValueSetValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        
        val name = queryParameters.name
        val valueSetName = queryParameters.valueSetName
        val project = queryParameters.project
        val selector = queryParameters.selector
        val targetGameType = selector.gameType ?: return
        
        val distinctInFile = selector.selectors.findIsInstance<ParadoxWithSearchScopeTypeSelector<*>>()
            ?.searchScopeType?.distinctInFile() ?: true
        
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            if(ParadoxFileManager.isLightFile(file)) return@p true
            val valueSetValueGroup = ParadoxValueSetValueIndex.getData(valueSetName, file, project)
            if(valueSetValueGroup.isNullOrEmpty()) return@p true
            val psiFile = file.toPsiFile(project) ?: return@p true
            
            if(name == null) {
                for(infos in valueSetValueGroup.values) {
                    for(info in infos) {
                        if(targetGameType == info.gameType) {
                            info.withFile(psiFile) { consumer.process(info) }
                        }
                        if(distinctInFile) break
                    }
                }
            } else {
                val infos = valueSetValueGroup[name] ?: return@p true
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

