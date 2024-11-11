package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.usageInfo.*

class ParadoxParameterSearch : ExtensibleQueryFactory<ParadoxParameterUsageInfo, ParadoxParameterSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val contextKey: String,
        override val selector: ChainedParadoxSelector<ParadoxParameterUsageInfo>
    ) : ParadoxSearchParameters<ParadoxParameterUsageInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxParameterUsageInfo, SearchParameters>>("icu.windea.pls.search.parameterSearch")
        @JvmField
        val INSTANCE = ParadoxParameterSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            contextKey: String,
            selector: ChainedParadoxSelector<ParadoxParameterUsageInfo>
        ): ParadoxQuery<ParadoxParameterUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, contextKey, selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            contextKey: String,
            selector: ChainedParadoxSelector<ParadoxParameterUsageInfo>
        ): ParadoxQuery<ParadoxParameterUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, contextKey, selector))
        }
    }
}
