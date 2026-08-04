package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本用法的查询器器。
 *
 * @see ParadoxInlineScriptUsageSearch
 */
@Optimized
class ParadoxInlineScriptUsageSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxInlineScriptUsageSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptUsageSearch.Parameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (ChronicleThreadContext.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, processor: Processor<in ParadoxScriptProperty>): Boolean {
        if (!context.isValid()) return true
        val expression = context.expression
        val indexKey = ChronicleIndexKeys.InlineScriptUsage
        if (expression == null) {
            return ChronicleIndexService.processElementsByKeys(indexKey, context.project, context.scope) { _, element ->
                processor.process(element)
            }
        } else {
            if (expression.isEmpty() || expression.isParameterized()) return true // 排除为空或者带参数的情况
            return ChronicleIndexService.processElements(indexKey, expression, context.project, context.scope) { element ->
                processor.process(element)
            }
        }
    }

    private fun ParadoxInlineScriptUsageSearch.Parameters.createContext(): Context {
        val scope = scope.withFileTypes(ParadoxScriptFileType) // optimize: restrict file types
        return Context(expression, gameType, project, scope)
    }

    private data class Context(
        val expression: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
