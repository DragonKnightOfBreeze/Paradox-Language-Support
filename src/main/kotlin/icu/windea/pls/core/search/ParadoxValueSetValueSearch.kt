package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.model.*

/**
 * 值集值的查询。（不涉及CWT规则文件中预定义的值）
 */
class ParadoxValueSetValueSearch : ExtensibleQueryFactory<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 名字。
     * @property valueSetNames 值集的名字。
     */
    class SearchParameters(
        val name: String?,
        val valueSetNames: Set<String>,
        override val selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
    ) : ParadoxSearchParameters<ParadoxValueSetValueInfo>
    
    companion object {
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxValueSetValueInfo, SearchParameters>>("icu.windea.pls.search.valueSetValueSearch")
        @JvmField val INSTANCE = ParadoxValueSetValueSearch()
        
        /**
         * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            valueSetName: String,
            selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
        ): ParadoxQuery<ParadoxValueSetValueInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name,  setOf(valueSetName), selector))
        }
        
        /**
         * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            valueSetNames: Set<String>,
            selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
        ): ParadoxQuery<ParadoxValueSetValueInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, valueSetNames, selector))
        }
        
        /**
         * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            valueSetName: String,
            selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
        ): ParadoxQuery<ParadoxValueSetValueInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, setOf(valueSetName), selector))
        }
        
        /**
         * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            valueSetNames: Set<String>,
            selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
        ): ParadoxQuery<ParadoxValueSetValueInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, valueSetNames, selector))
        }
    }
}
