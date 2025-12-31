package icu.windea.pls.lang.search

import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 本地化的查询。
 */
class ParadoxLocalisationSearch : ExtensibleQueryFactory<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 本地化的名字。
     * @property type 本地化的类型（所有/普通/同步）。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val name: String?,
        val type: ParadoxLocalisationType,
        override val selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ) : ParadoxSearchParameters<ParadoxLocalisationProperty>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.search.localisationSearch")
        @JvmField
        val INSTANCE = ParadoxLocalisationSearch()

        /**
         *  @see ParadoxLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: ParadoxLocalisationType,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, type, selector))
        }

        /**
         *  @see ParadoxLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun searchNormal(
            name: String?,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return search(name, ParadoxLocalisationType.Normal, selector)
        }

        /**
         *  @see ParadoxLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun searchSynced(
            name: String?,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return search(name, ParadoxLocalisationType.Synced, selector)
        }

        @JvmStatic
        fun processVariants(
            type: ParadoxLocalisationType,
            prefixMatcher: PrefixMatcher,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
            processor: Processor<ParadoxLocalisationProperty>
        ): Boolean {
            val indexKey = when (type) {
                ParadoxLocalisationType.Normal -> PlsIndexKeys.LocalisationName
                ParadoxLocalisationType.Synced -> PlsIndexKeys.SyncedLocalisationName
            }
            return PlsIndexService.processVariants(indexKey, prefixMatcher, selector, processor)
        }

        @JvmStatic
        fun processVariantsNormal(
            prefixMatcher: PrefixMatcher,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
            processor: Processor<ParadoxLocalisationProperty>
        ): Boolean {
            return processVariants(ParadoxLocalisationType.Normal, prefixMatcher, selector, processor)
        }

        @JvmStatic
        fun processVariantsSynced(
            prefixMatcher: PrefixMatcher,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
            processor: Processor<ParadoxLocalisationProperty>
        ): Boolean {
            return processVariants(ParadoxLocalisationType.Synced, prefixMatcher, selector, processor)
        }
    }
}
