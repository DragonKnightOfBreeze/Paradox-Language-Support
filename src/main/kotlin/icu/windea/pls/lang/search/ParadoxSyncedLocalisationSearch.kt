package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 同步本地化的查询。
 */
class ParadoxSyncedLocalisationSearch : ExtensibleQueryFactory<ParadoxLocalisationProperty, ParadoxSyncedLocalisationSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 同步本地化的名字。
     */
    class SearchParameters(
        val name: String?,
        override val selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ) : ParadoxSearchParameters<ParadoxLocalisationProperty>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.search.syncedLocalisationSearch")
        @JvmField
        val INSTANCE = ParadoxSyncedLocalisationSearch()

        /**
         * @see ParadoxSyncedLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }

    }
}
