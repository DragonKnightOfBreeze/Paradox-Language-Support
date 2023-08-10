package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.*
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
         * 基于同步本地化名字索引，根据关键字和推断的语言区域遍历所有的同步本地化（localisation_synced），并按照本地化的键进行去重。
         *
         * 用于优化代码提示的性能。
         */
        @JvmStatic
        fun processVariants(
            keyword: String,
            selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
            processor: Processor<ParadoxLocalisationProperty>
        ): Boolean {
            //保证返回结果的名字的唯一性
            val project = selector.project
            val scope = selector.scope
            return ParadoxSyncedLocalisationNameIndexKey.processFirstElementByKeys(
                project, scope,
                predicate = { element -> selector.select(element) },
                keyPredicate = { key -> key.matchesKeyword(keyword) },
                getDefaultValue = { selector.defaultValue() },
                resetDefaultValue = { selector.resetDefaultValue() },
                processor = { processor.process(it) }
            )
        }
    }
}