package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 同步本地化的查询器。
 */
class ParadoxSyncedLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxSyncedLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxSyncedLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            if(queryParameters.name == null) {
                ParadoxSyncedLocalisationNameIndex.KEY.processAllElementsByKeys(project, scope) p@{ _, it ->
                    consumer.process(it)
                }
            } else {
                ParadoxSyncedLocalisationNameIndex.KEY.processAllElements(queryParameters.name, project, scope) p@{
                    consumer.process(it)
                }
            }
        }
    }
}