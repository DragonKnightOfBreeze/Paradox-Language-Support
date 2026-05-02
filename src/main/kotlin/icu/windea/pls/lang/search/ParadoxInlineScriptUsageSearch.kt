package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本用法的查询。
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxInlineScriptUsageSearch.Parameters>(EP_NAME) {
    /**
     * 内联脚本用法的查询参数。
     *
     * @property expression 内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    class Parameters(
        val expression: String?,
        override val selector: ParadoxSearchSelector<ParadoxScriptProperty>
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField val INSTANCE = ParadoxInlineScriptUsageSearch()

        /**
         * @see ParadoxInlineScriptUsageSearch.Parameters
         */
        @JvmStatic
        fun search(
            expression: String?,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>,
        ): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(expression, selector))
        }
    }
}

