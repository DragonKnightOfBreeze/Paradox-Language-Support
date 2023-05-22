package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
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
            val data = ParadoxValueSetValueIndex.getData(file, project)
            //use distinct data if possible to optimize performance
            val valueSetValueInfos = when {
                distinctInFile -> data.valueSetValueInfoGroup[valueSetName]
                else -> data.distinctValueSetValueInfoGroup[valueSetName]
            }
            if(valueSetValueInfos.isNullOrEmpty()) return@p true
            
            val psiFile = file.toPsiFile(project) ?: return@p true
            if(name == null) {
                valueSetValueInfos.values.forEach { infos ->
                    infos.forEachFast { info ->
                        if(targetGameType == info.gameType) {
                            info.withFile(psiFile) { consumer.process(info) }
                        }
                    }
                }
            } else {
                val infos = valueSetValueInfos[name] ?: return@p true
                infos.forEachFast { info ->
                    if(targetGameType == info.gameType) {
                        info.withFile(psiFile) { consumer.process(info) }
                    }
                }
            }
            true
        }, scope)

    }
}

