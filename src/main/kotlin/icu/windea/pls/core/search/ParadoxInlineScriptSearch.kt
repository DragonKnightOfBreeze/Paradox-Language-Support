package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*

/**
 * 内联脚本使用的查询。
 */
class ParadoxInlineScriptSearch: ExtensibleQueryFactory<ParadoxInlineScriptUsageInfo, ParadoxInlineScriptSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本的路径表达式。
     */
    class SearchParameters(
        val expression: String,
        override val selector: ChainedParadoxSelector<ParadoxInlineScriptUsageInfo>
    ) : ParadoxSearchParameters<ParadoxInlineScriptUsageInfo>
    
    companion object {
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxInlineScriptUsageInfo, SearchParameters>>("icu.windea.pls.paradoxInlineScriptSearch")
        @JvmField val INSTANCE = ParadoxInlineScriptSearch()
        
        /**
         * @see icu.windea.pls.core.search.ParadoxInlineScriptSearch.SearchParameters
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

