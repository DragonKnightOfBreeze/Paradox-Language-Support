package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.processAllElementsByKeys
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本使用的查询器。
 */
class ParadoxInlineScriptUsageSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxInlineScriptUsageSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptUsageSearch.SearchParameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val inlineScriptExpression = queryParameters.inlineScriptExpression
        val project = queryParameters.project
        processQueryForInlineScriptUsages(inlineScriptExpression, project, scope) { element -> consumer.process(element) }
    }

    private fun processQueryForInlineScriptUsages(
        inlineScriptExpression: String?,
        project: Project,
        scope: GlobalSearchScope,
        processor: Processor<ParadoxScriptProperty>
    ): Boolean {
        val indexKey = PlsIndexKeys.InlineScriptUsage
        if (inlineScriptExpression == null) {
            return indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return true // 排除为空或者带参数的情况
            return indexKey.processAllElements(inlineScriptExpression, project, scope) { element -> processor.process(element) }
        }
    }
}
