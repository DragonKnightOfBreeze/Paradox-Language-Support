package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.usageInfo.*

/**
 * 内联脚本使用的查询。
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxInlineScriptUsageInfo.Compact, ParadoxInlineScriptUsageSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本的路径表达式。
     */
    class SearchParameters(
        val expression: String,
        override val selector: ChainedParadoxSelector<ParadoxInlineScriptUsageInfo.Compact>
    ) : ParadoxSearchParameters<ParadoxInlineScriptUsageInfo.Compact>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxInlineScriptUsageInfo.Compact, SearchParameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField
        val INSTANCE = ParadoxInlineScriptUsageSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            expression: String,
            selector: ChainedParadoxSelector<ParadoxInlineScriptUsageInfo.Compact>
        ): ParadoxQuery<ParadoxInlineScriptUsageInfo.Compact, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(expression, selector))
        }
    }
}

