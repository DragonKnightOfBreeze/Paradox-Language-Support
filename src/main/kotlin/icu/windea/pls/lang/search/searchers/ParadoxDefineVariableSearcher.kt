package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.base.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxDefineVariableKey
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间的查询器。
 */
class ParadoxDefineVariableSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxDefineVariableSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxDefineVariableSearch.Parameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt") // optimized
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxScriptProperty>): Boolean {
        if (!context.isValid()) return true
        if (context.namespace != null && context.variable != null) {
            val key = ParadoxDefineVariableKey(context.namespace, context.variable)
            return PlsIndexService.processElements(PlsIndexKeys.DefineVariable, key, context.project, context.scope) { element ->
                consumer.process(element)
            }
        } else if (context.namespace != null) {
            return PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, context.project, context.scope, { it.namespace == context.namespace }) { _, element ->
                consumer.process(element)
            }
        } else if (context.variable != null) {
            return PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, context.project, context.scope, { it.variable == context.variable }) { _, element ->
                consumer.process(element)
            }
        } else {
            return PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, context.project, context.scope) { _, element ->
                consumer.process(element)
            }
        }
    }

    private fun ParadoxDefineVariableSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(namespace, variable, gameType, project, scope)
    }

    private data class Context(
        val namespace: String?,
        val variable: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
