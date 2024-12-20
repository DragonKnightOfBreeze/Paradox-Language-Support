package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.indexInfo.*

class ParadoxParameterSearch : ExtensibleQueryFactory<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val contextKey: String,
        override val selector: ChainedParadoxSelector<ParadoxParameterIndexInfo>
    ) : ParadoxSearchParameters<ParadoxParameterIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxParameterIndexInfo, SearchParameters>>("icu.windea.pls.search.parameterSearch")
        @JvmField
        val INSTANCE = ParadoxParameterSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            contextKey: String,
            selector: ChainedParadoxSelector<ParadoxParameterIndexInfo>
        ): ParadoxQuery<ParadoxParameterIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, contextKey, selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            contextKey: String,
            selector: ChainedParadoxSelector<ParadoxParameterIndexInfo>
        ): ParadoxQuery<ParadoxParameterIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, contextKey, selector))
        }
    }
}
