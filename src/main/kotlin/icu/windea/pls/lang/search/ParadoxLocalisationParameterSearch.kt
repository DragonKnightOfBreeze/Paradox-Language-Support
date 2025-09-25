package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.indexInfo.ParadoxLocalisationParameterIndexInfo

class ParadoxLocalisationParameterSearch : ExtensibleQueryFactory<ParadoxLocalisationParameterIndexInfo, ParadoxLocalisationParameterSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val localisationName: String,
        override val selector: ChainedParadoxSelector<ParadoxLocalisationParameterIndexInfo>
    ) : ParadoxSearchParameters<ParadoxLocalisationParameterIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxLocalisationParameterIndexInfo, SearchParameters>>("icu.windea.pls.search.localisationParameterSearch")
        @JvmField
        val INSTANCE = ParadoxLocalisationParameterSearch()

        /**
         * @see ParadoxLocalisationParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            localisationName: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationParameterIndexInfo>
        ): ParadoxQuery<ParadoxLocalisationParameterIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, localisationName, selector))
        }
    }
}
