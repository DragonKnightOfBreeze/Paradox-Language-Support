package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.index.ParadoxParameterIndexInfo

/**
 * 参数的查询。
 */
class ParadoxParameterSearch : ExtensibleQueryFactory<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 参数的名字。
     * @property contextKey 上下文键（如 `scripted_trigger@x`、`inline_script@x`）。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val name: String?,
        val contextKey: String,
        override val selector: ChainedParadoxSelector<ParadoxParameterIndexInfo>
    ) : ParadoxSearchParameters<ParadoxParameterIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxParameterIndexInfo, SearchParameters>>("icu.windea.pls.search.parameterSearch")
        @JvmField
        val INSTANCE = ParadoxParameterSearch()

        /**
         * @see ParadoxParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            contextKey: String,
            selector: ChainedParadoxSelector<ParadoxParameterIndexInfo>
        ): ParadoxQuery<ParadoxParameterIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, contextKey, selector))
        }
    }
}
