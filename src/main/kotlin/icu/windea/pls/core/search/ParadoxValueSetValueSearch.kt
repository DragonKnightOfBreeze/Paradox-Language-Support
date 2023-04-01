package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.model.*

/**
 * 值集值的查询。（不涉及CWT规则文件中预定义的值）
 */
class ParadoxValueSetValueSearch : ExtensibleQueryFactory<ParadoxValueSetValueInfo, ParadoxValueSetValueSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 名字。
     * @property valueSetName 值集的名字。
     */
    class SearchParameters(
        val name: String?,
        val valueSetName: String,
        override val selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
    ) : ParadoxSearchParameters<ParadoxValueSetValueInfo>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxValueSetValueInfo, SearchParameters>>("icu.windea.pls.paradoxValueSetValueSearch")
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
            return INSTANCE.createParadoxQuery(SearchParameters(name, valueSetName, selector))
        }
        
        /**
         * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            valueSetName: String,
            selector: ChainedParadoxSelector<ParadoxValueSetValueInfo>
        ): ParadoxQuery<ParadoxValueSetValueInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, valueSetName, selector))
        }
    }
}
