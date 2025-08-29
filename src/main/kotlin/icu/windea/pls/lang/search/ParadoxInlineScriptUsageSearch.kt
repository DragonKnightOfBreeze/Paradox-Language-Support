package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo

/**
 * 内联脚本使用的查询。
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxInlineScriptUsageIndexInfo.Compact, ParadoxInlineScriptUsageSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本的路径表达式。
     */
    class SearchParameters(
        val expression: String,
        override val selector: ChainedParadoxSelector<ParadoxInlineScriptUsageIndexInfo.Compact>
    ) : ParadoxSearchParameters<ParadoxInlineScriptUsageIndexInfo.Compact>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxInlineScriptUsageIndexInfo.Compact, SearchParameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField
        val INSTANCE = ParadoxInlineScriptUsageSearch()

        /**
         * @see ParadoxInlineScriptUsageSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            expression: String,
            selector: ChainedParadoxSelector<ParadoxInlineScriptUsageIndexInfo.Compact>
        ): ParadoxQuery<ParadoxInlineScriptUsageIndexInfo.Compact, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(expression, selector))
        }
    }
}

