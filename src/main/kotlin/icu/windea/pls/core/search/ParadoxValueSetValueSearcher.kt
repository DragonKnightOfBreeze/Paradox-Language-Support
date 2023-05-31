package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.model.*

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
        val gameType = selector.gameType ?: return
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxValueSetValueFastIndex.NAME, valueSetNames, scope, null, null) p@{ file ->
                ProgressManager.checkCanceled()
                if(selectGameType(file) != gameType) return@p true //check game type at file level
                val fileData = FileBasedIndex.getInstance().getFileData(ParadoxValueSetValueFastIndex.NAME, file, project)
                valueSetNames.forEach f@{ valueSetName ->
                    val valueSetValueInfoList = fileData[valueSetName]
                    if(valueSetValueInfoList.isNullOrEmpty()) return@f
                    valueSetValueInfoList.forEachFast { info ->
                        if(name == null || name == info.name) {
                            val r = consumer.process(info)
                            if(!r) return@p false
                        }
                    }
                }
                true
            }
        }
    }
}

