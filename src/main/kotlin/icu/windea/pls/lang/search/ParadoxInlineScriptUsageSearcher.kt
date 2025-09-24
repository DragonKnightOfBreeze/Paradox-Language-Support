package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本使用的查询器。
 */
class ParadoxInlineScriptUsageSearcher : QueryExecutorBase<ParadoxInlineScriptUsageIndexInfo.Compact, ParadoxInlineScriptUsageSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxInlineScriptUsageSearch.SearchParameters, consumer: Processor<in ParadoxInlineScriptUsageIndexInfo.Compact>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val expression = queryParameters.expression
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType

        if (expression.isNotEmpty() && expression.isParameterized()) return // skip if expression is parameterized

        // 使用 StubIndex：expression -> ParadoxScriptProperty（inline_script 使用位置）
        if (expression.isNotEmpty()) {
            val props = StubIndex.getElements(ParadoxIndexKeys.InlineScriptUsageByExpression, expression, project, scope, ParadoxScriptProperty::class.java)
            publishGrouped(props, expression, gameType, consumer)
        } else {
            // 遍历所有 key，但限定在 scope 内
            val keys = StubIndex.getInstance().getAllKeys(ParadoxIndexKeys.InlineScriptUsageByExpression, project)
            for (key in keys) {
                val props = StubIndex.getElements(ParadoxIndexKeys.InlineScriptUsageByExpression, key, project, scope, ParadoxScriptProperty::class.java)
                publishGrouped(props, key, gameType, consumer)
            }
        }
    }

    private fun publishGrouped(props: Collection<ParadoxScriptProperty>, expression: String, gameType: icu.windea.pls.model.ParadoxGameType?, consumer: Processor<in ParadoxInlineScriptUsageIndexInfo.Compact>) {
        if (props.isEmpty()) return
        val byFile = props.groupBy { it.containingFile.virtualFile }
        for ((vFile, list) in byFile) {
            if (vFile == null) continue
            val fileGameType = selectGameType(vFile)
            if (gameType != null && fileGameType != gameType) continue
            val offsets = sortedSetOf<Int>()
            for (p in list) offsets += p.textOffset
            val effectiveGameType = gameType ?: fileGameType ?: icu.windea.pls.model.ParadoxGameType.Core
            val compact = ParadoxInlineScriptUsageIndexInfo.Compact(expression, offsets, effectiveGameType)
            compact.virtualFile = vFile
            val r = consumer.process(compact)
            if (!r) return
        }
    }
}
