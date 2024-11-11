package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.usageInfo.*

class ParadoxLocalisationParameterSearch : ExtensibleQueryFactory<ParadoxLocalisationParameterUsageInfo, ParadoxLocalisationParameterSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val localisationName: String,
        override val selector: ChainedParadoxSelector<ParadoxLocalisationParameterUsageInfo>
    ) : ParadoxSearchParameters<ParadoxLocalisationParameterUsageInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationParameterUsageInfo, SearchParameters>>("icu.windea.pls.search.localisationParameterSearch")
        @JvmField
        val INSTANCE = ParadoxLocalisationParameterSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxLocalisationParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            localisationName: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationParameterUsageInfo>
        ): ParadoxQuery<ParadoxLocalisationParameterUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, localisationName, selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxLocalisationParameterSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            localisationName: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationParameterUsageInfo>
        ): ParadoxQuery<ParadoxLocalisationParameterUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, localisationName, selector))
        }
    }
}
