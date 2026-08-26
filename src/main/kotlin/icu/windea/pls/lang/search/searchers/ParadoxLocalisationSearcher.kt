package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.hasState
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.index.ParadoxMergedIndexThreadContext
import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.getConstraint
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 本地化的查询器。
 *
 * @see ParadoxLocalisationSearch
 */
@Optimized
class ParadoxLocalisationSearcher : QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationSearch.Parameters, consumer: Processor<in ParadoxLocalisationProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (ParadoxMergedIndexThreadContext.isResolving.hasState()) return

        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, processor: Processor<in ParadoxLocalisationProperty>): Boolean {
        if (!context.isValid()) return true
        val constraint = context.constraint
        val indexKey = constraint?.indexKey ?: when (context.type) {
            ParadoxLocalisationType.Normal -> ChronicleIndexKeys.LocalisationName
            ParadoxLocalisationType.Synced -> ChronicleIndexKeys.SyncedLocalisationName
        }
        val ignoreCase = constraint?.ignoreCase == true
        val name = if (ignoreCase) context.name?.lowercase() else context.name
        val r = if (name == null) {
            ChronicleIndexService.processElementsByKeys(indexKey, context.project, context.scope) { _, element -> processor.process(element) }
        } else {
            ChronicleIndexService.processElements(indexKey, name, context.project, context.scope) { element -> processor.process(element) }
        }
        if (!r) return false

        // fallback for inferred constraints
        if (constraint?.inferred == true) {
            return processQuery(context.copy(constraint = null), processor)
        }

        return true
    }

    private fun ParadoxLocalisationSearch.Parameters.createContext(): Context {
        val constraint = selector.getConstraint() as? ParadoxLocalisationIndexConstraint // extract index constraint from the selector
        val scope = scope.withFileTypes(ParadoxLocalisationFileType) // optimize: restrict file types
        return Context(name, type, constraint, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val type: ParadoxLocalisationType,
        val constraint: ParadoxLocalisationIndexConstraint?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
