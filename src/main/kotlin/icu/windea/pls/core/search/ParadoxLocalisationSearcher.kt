package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的查询器。
 */
class ParadoxLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        
        val constraint = queryParameters.selector.getConstraint()
        val indexKey = constraint.indexKey
        val name = if(constraint.ignoreCase) queryParameters.name?.lowercase() else queryParameters.name
        if(name == null) {
            indexKey.processAllElementsByKeys(project, scope) { _, it ->
                consumer.process(it)
            }
        } else {
            indexKey.processAllElements(name, project, scope) {
                consumer.process(it)
            }
        }
    }
}
