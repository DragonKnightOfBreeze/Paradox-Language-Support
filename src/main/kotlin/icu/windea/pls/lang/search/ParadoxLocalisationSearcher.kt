package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.processAllElementsByKeys
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxIndexConstraint

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

        processQueryForLocalisations(queryParameters.name, project, scope, constraint) { element -> consumer.process(element) }
    }

    private fun processQueryForLocalisations(
        name: String?,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxLocalisationProperty>?,
        processor: Processor<ParadoxLocalisationProperty>
    ): Boolean {
        val indexKey = constraint?.indexKey ?: ParadoxIndexKeys.LocalisationName
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val r = if (finalName == null) {
            indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            indexKey.processAllElements(finalName, project, scope) { element -> processor.process(element) }
        }
        if (!r) return false

        // fallback for inferred constraints
        if (constraint != null && constraint.inferred) {
            return processQueryForLocalisations(name, project, scope, null, processor)
        }

        return true
    }
}
