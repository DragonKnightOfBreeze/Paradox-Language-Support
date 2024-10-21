package icu.windea.pls.lang.search

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*

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
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.search.localisationSearch")
        @JvmField
        val INSTANCE = ParadoxLocalisationSearch()

        /**
         *  @see icu.windea.pls.lang.search.ParadoxLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }

        /**
         *  @see icu.windea.pls.lang.search.ParadoxLocalisationSearch.SearchParameters
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
            return ParadoxLocalisationNameIndex.KEY.processFirstElementByKeys(
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

