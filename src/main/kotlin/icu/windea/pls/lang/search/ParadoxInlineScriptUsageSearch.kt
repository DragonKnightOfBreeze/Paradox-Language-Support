package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxInlineScriptUsageSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.createParadoxQuery
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本用法的查询。
 *
 * @see ParadoxInlineScriptUsageSearcher
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxInlineScriptUsageSearch.Parameters>(EP_NAME) {
    /**
     * 内联脚本用法的查询参数。
     *
     * @property expression 内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    data class Parameters(
        val expression: String?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxScriptProperty>(project, context)

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField val INSTANCE = ParadoxInlineScriptUsageSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(expression: String?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.createParadoxQuery(Parameters(expression, selector))
        }
    }
}

