package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值的命名空间的查询器。
 */
class ParadoxDefineNamespaceSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxDefineNamespaceSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDefineNamespaceSearch.Parameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt") // optimized
        val context = createContext(queryParameters, scope)
        if (!context.isValid()) return
        processInternal(context, consumer)
    }

    private fun processInternal(context: Context, consumer: Processor<in ParadoxScriptProperty>) {
        val project = context.project
        val scope = context.scope
        if (context.namespace == null) {
            PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineNamespace, project, scope) { _, element ->
                consumer.process(element)
            }
        } else {
            if (context.namespace.isEmpty()) return
            PlsIndexService.processElements(PlsIndexKeys.DefineNamespace, context.namespace, project, scope) { element ->
                consumer.process(element)
            }
        }
    }

    private fun createContext(p: ParadoxDefineNamespaceSearch.Parameters, scope: GlobalSearchScope = p.scope): Context {
        return Context(p.namespace, p.gameType, p.project, scope)
    }

    private data class Context(
        val namespace: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
