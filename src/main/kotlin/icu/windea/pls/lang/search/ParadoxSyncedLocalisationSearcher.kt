package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 同步本地化的查询器。
 */
class ParadoxSyncedLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxSyncedLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxSyncedLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return

        val name = queryParameters.name
        processQueryForSyncedLocalisations(name, project, scope) { element -> consumer.process(element) }
    }

    private fun processQueryForSyncedLocalisations(name: String?, project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxLocalisationProperty>): Boolean {
        val indexKey = PlsIndexKeys.SyncedLocalisationName
        if (name == null) {
            return PlsIndexService.processElementsByKeys(indexKey, project, scope) { _, element -> processor.process(element) }
        } else {
            return PlsIndexService.processElements(indexKey, name, project, scope) { element -> processor.process(element) }
        }
    }
}
