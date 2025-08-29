package icu.windea.pls.lang.search

import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.processFirstElementByKeys
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 本地化的查询。
 */
class ParadoxLocalisationSearch : ExtensibleQueryFactory<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 本地化的名字。
     */
    class SearchParameters(
        val name: String?,
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
            name: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }

        /**
         *  @see ParadoxLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, selector))
        }

        /**
         * 用于优化代码提示的性能。
         */
        @JvmStatic
        fun processVariants(
            prefixMatcher: PrefixMatcher,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
            processor: Processor<ParadoxLocalisationProperty>
        ): Boolean {
            val project = selector.project
            val scope = selector.scope
            //保证返回结果的名字的唯一性
            return ParadoxIndexKeys.LocalisationName.processFirstElementByKeys(
                project, scope,
                keyPredicate = { key -> prefixMatcher.prefixMatches(key) },
                predicate = { element -> selector.selectOne(element) },
                getDefaultValue = { selector.defaultValue() },
                resetDefaultValue = { selector.resetDefaultValue() },
                processor = { processor.process(it) }
            )
        }
    }
}

