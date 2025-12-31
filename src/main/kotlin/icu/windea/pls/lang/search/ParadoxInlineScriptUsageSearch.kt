package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本用法的查询。
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxInlineScriptUsageSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val expression: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptProperty>
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, SearchParameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField
        val INSTANCE = ParadoxInlineScriptUsageSearch()

        /**
         * @see ParadoxInlineScriptUsageSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            expression: String?,
            selector: ChainedParadoxSelector<ParadoxScriptProperty>
        ): ParadoxQuery<ParadoxScriptProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(expression, selector))
        }
    }
}

