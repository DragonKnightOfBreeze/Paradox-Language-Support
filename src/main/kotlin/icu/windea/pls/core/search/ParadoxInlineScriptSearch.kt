package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.model.*

/**
 * 内联脚本调用的查询。
 */
class ParadoxInlineScriptSearch: ExtensibleQueryFactory<ParadoxInlineScriptInfo, ParadoxInlineScriptSearch.SearchParameters>(EP_NAME) {
    /**
     * @property expression 内联脚本的路径表达式。
     */
    class SearchParameters(
        val expression: String,
        override val selector: ChainedParadoxSelector<ParadoxInlineScriptInfo>
    ) : ParadoxSearchParameters<ParadoxInlineScriptInfo>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxInlineScriptInfo, SearchParameters>>("icu.windea.pls.paradoxInlineScriptSearch")
        @JvmField val INSTANCE = ParadoxInlineScriptSearch()
        
        /**
         * @see icu.windea.pls.core.search.ParadoxInlineScriptSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            expression: String,
            selector: ChainedParadoxSelector<ParadoxInlineScriptInfo>
        ): ParadoxQuery<ParadoxInlineScriptInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(expression, selector))
        }
    }
}
