package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*

/**
 * 本地化的查询器。
 */
class ParadoxLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        //#141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val constraint = queryParameters.selector.getConstraint()
        processQueryForLocalisations(queryParameters.name, project, scope, constraint) { element ->
            consumer.process(element)
        }
    }

    private fun processQueryForLocalisations(
        name: String?,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxLocalisationProperty>?,
        processor: Processor<ParadoxLocalisationProperty>
    ): Boolean {
        val indexKey = constraint?.indexKey ?: ParadoxIndexManager.LocalisationNameKey
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        if (finalName == null) {
            return indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return indexKey.processAllElements(finalName, project, scope) { element -> processor.process(element) }
        }
    }
}
