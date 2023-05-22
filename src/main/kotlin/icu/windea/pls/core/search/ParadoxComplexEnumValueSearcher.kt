package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
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
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            doProcessFiles(scope) p@{ file ->
                ProgressManager.checkCanceled()
                if(ParadoxFileManager.isLightFile(file)) return@p true
                val data = ParadoxComplexEnumValueIndex.getData(file, project)
                //use distinct data if possible to optimize performance
                val complexEnumValueInfos = when {
                    distinctInFile -> data.distinctComplexEnumValueInfoGroup[enumName]
                    else -> data.complexEnumValueInfoGroup[enumName]
                }
                if(complexEnumValueInfos.isNullOrEmpty()) return@p true
                
                val psiFile = file.toPsiFile(project) ?: return@p true
                if(name == null) {
                    complexEnumValueInfos.values.forEach { infos ->
                        infos.forEachFast { info ->
                            if(targetGameType == info.gameType) {
                                info.withFile(psiFile) { consumer.process(info) }
                            }
                        }
                    }
                } else {
                    val infos = complexEnumValueInfos[name] ?: return@p true
                    infos.forEachFast { info ->
                        if(targetGameType == info.gameType) {
                            info.withFile(psiFile) { consumer.process(info) }
                        }
                    }
                }
                true
            }
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}