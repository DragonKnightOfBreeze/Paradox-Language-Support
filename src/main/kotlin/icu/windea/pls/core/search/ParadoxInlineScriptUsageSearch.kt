package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*

/**
 * 内联脚本使用的查询。
 */
class ParadoxInlineScriptUsageSearch : ExtensibleQueryFactory<ParadoxInlineScriptUsageInfo, ParadoxInlineScriptUsageSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本的路径表达式。
     */
    class SearchParameters(
        val expression: String,
        override val selector: ChainedParadoxSelector<ParadoxInlineScriptUsageInfo>
    ) : ParadoxSearchParameters<ParadoxInlineScriptUsageInfo>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxInlineScriptUsageInfo, SearchParameters>>("icu.windea.pls.search.inlineScriptUsageSearch")
        @JvmField val INSTANCE = ParadoxInlineScriptUsageSearch()
        
        /**
         * @see icu.windea.pls.core.search.ParadoxInlineScriptUsageSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            expression: String,
            selector: ChainedParadoxSelector<ParadoxInlineScriptUsageInfo>
        ): ParadoxQuery<ParadoxInlineScriptUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(expression, selector))
        }
    }
}

