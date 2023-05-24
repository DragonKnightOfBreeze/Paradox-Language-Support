package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.tool.*

/**
 * 值集值的查询器。
 */
class ParadoxValueSetValueSearcher : QueryExecutorBase<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxValueSetValueSearch.SearchParameters, consumer: Processor<in ParadoxValueSetValueInfo>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        
        val name = queryParameters.name
        val valueSetNames = queryParameters.valueSetNames
        val project = queryParameters.project
        val selector = queryParameters.selector
        val targetGameType = selector.gameType ?: return
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{ //perf: 58% 100%
            doProcessFiles(scope) p@{ file ->
                ProgressManager.checkCanceled()
                if(ParadoxFileManager.isLightFile(file)) return@p true
                if(selectGameType(file) != targetGameType) return@p true //check game type at file level
                
                val data = ParadoxValueSetValueFastIndex.getData(file, project)
                val valueSetValueInfoGroup = data.valueSetValueInfoGroup
                if(valueSetValueInfoGroup.isEmpty()) return@p true
                valueSetNames.forEach f@{ valueSetName ->
                    val valueSetValueInfoList = valueSetValueInfoGroup[valueSetName]
                    if(valueSetValueInfoList.isNullOrEmpty()) return@f
                    valueSetValueInfoList.forEach { info ->
                        if(name == null || name == info.name) {
                            consumer.process(info)
                        }
                    }
                }
                
                //val data = ParadoxValueSetValueIndex.getData(file, project) //perf: 49% 86%
                ////use distinct data if possible to optimize performance
                //valueSetNames.forEach f@{ valueSetName ->
                //    val valueSetValueInfos = when {
                //        distinctInFile -> data.distinctValueSetValueInfoGroup[valueSetName] //perf: 8% 100%
                //        else -> data.valueSetValueInfoGroup[valueSetName]
                //    }
                //    if(valueSetValueInfos.isNullOrEmpty()) return@f
                //    
                //    if(name == null) {
                //        valueSetValueInfos.values.forEach { infos ->
                //            infos.forEachFast { info ->
                //                consumer.process(info)
                //            }
                //        }
                //    } else {
                //        val infos = valueSetValueInfos[name] ?: return@f
                //        infos.forEachFast { info ->
                //            consumer.process(info)
                //        }
                //    }
                //}
                
                true
            }
        }
    }
    
    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>) {
        ProgressManager.checkCanceled()
        FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
        ProgressManager.checkCanceled()
        FileTypeIndex.processFiles(ParadoxLocalisationFileType, processor, scope)
    }
}

