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
 * 复杂枚举值的查询器。
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
        val gameType = selector.gameType ?: return
        
        //val distinctInFile = selector.selectors.findIsInstance<ParadoxWithSearchScopeTypeSelector<*>>()?.searchScopeType?.distinctInFile() ?: true
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            FileBasedIndex.getInstance().processValues(ParadoxComplexEnumValueIndex.NAME, enumName, null, p@{ file, value->
                ProgressManager.checkCanceled()
                if(selectGameType(file) != gameType) return@p true //check game type at file level
                val complexEnumValueInfoList = value
                if(complexEnumValueInfoList.isEmpty()) return@p true
                complexEnumValueInfoList.forEachFast { info ->
                    if(name == null || name == info.name) {
                        val r = consumer.process(info)
                        if(!r) return@p false
                    }
                }
                true
            }, scope)
        }
    }
}