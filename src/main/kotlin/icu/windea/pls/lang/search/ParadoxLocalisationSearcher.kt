package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint

/**
 * 本地化的查询器。
 */
class ParadoxLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxLocalisationFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val name = queryParameters.name
        val type = queryParameters.type
        val constraint = queryParameters.selector.getConstraint() as? ParadoxLocalisationIndexConstraint
        processQueryForLocalisations(name, type, project, scope, constraint) { element -> consumer.process(element) }
    }

    private fun processQueryForLocalisations(
        name: String?,
        type: ParadoxLocalisationType,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxLocalisationIndexConstraint?,
        processor: Processor<ParadoxLocalisationProperty>
    ): Boolean {
        val indexKey = constraint?.indexKey ?: when (type) {
            ParadoxLocalisationType.Normal -> PlsIndexKeys.LocalisationName
            ParadoxLocalisationType.Synced -> PlsIndexKeys.SyncedLocalisationName
        }
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val r = if (finalName == null) {
            PlsIndexService.processElementsByKeys(indexKey, project, scope) { _, element -> processor.process(element) }
        } else {
            PlsIndexService.processElements(indexKey, finalName, project, scope) { element -> processor.process(element) }
        }
        if (!r) return false

        // fallback for inferred constraints
        if (constraint != null && constraint.inferred) {
            return processQueryForLocalisations(name, type, project, scope, null, processor)
        }

        return true
    }
}
