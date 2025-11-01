package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.index.ParadoxParameterIndexInfo

class ParadoxParameterSearch : ExtensibleQueryFactory<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>(EP_NAME) {
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
