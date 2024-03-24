package icu.windea.pls.lang.search

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*

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
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.search.syncedLocalisationSearch")
        @JvmField val INSTANCE = ParadoxSyncedLocalisationSearch()
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxSyncedLocalisationSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
        ): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxSyncedLocalisationSearch.SearchParameters
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
            //保证返回结果的名字的唯一性
            val project = selector.project
            val scope = selector.scope
            return ParadoxSyncedLocalisationNameIndexKey.processFirstElementByKeys(
                project, scope,
                keyPredicate = { key -> prefixMatcher.prefixMatches(key) },
                predicate = { element -> selector.select(element) },
                getDefaultValue = { selector.defaultValue() },
                resetDefaultValue = { selector.resetDefaultValue() },
                processor = { processor.process(it) }
            )
        }
    }
}