package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 同步本地化的查询器。
 */
class ParadoxSyncedLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxSyncedLocalisationSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxSyncedLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        //#141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if(PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if(queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val name = queryParameters.name
        processQueryForSyncedLocalisations(name, project, scope) { element ->
            consumer.process(element)
        }
    }

    private fun processQueryForSyncedLocalisations(name: String?, project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxLocalisationProperty>): Boolean {
        val indexKey = ParadoxIndexKeys.SyncedLocalisationName
        if (name == null) {
            return indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return indexKey.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
