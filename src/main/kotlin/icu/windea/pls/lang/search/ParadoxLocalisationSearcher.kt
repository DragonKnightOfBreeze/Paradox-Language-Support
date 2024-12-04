package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的查询器。
 */
class ParadoxLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val constraint = queryParameters.selector.getConstraint()
        val indexKey = constraint.indexKey
        val name = if (constraint.ignoreCase) queryParameters.name?.lowercase() else queryParameters.name
        doProcessAllElements(indexKey, name, project, scope) { element ->
            consumer.process(element)
        }
    }

    private fun doProcessAllElements(
        indexKey: StubIndexKey<String, ParadoxLocalisationProperty>, name: String?,
        project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxLocalisationProperty>
    ): Boolean {
        if (name == null) {
            return indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return indexKey.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
