package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.usageInfo.*

/**
 * 复杂枚举的查询。
 */
class ParadoxComplexEnumValueSearch : ExtensibleQueryFactory<ParadoxComplexEnumValueUsageInfo, ParadoxComplexEnumValueSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 名字。
     * @property enumName 枚举的名字。
     */
    class SearchParameters(
        val name: String?,
        val enumName: String,
        override val selector: ChainedParadoxSelector<ParadoxComplexEnumValueUsageInfo>
    ) : ParadoxSearchParameters<ParadoxComplexEnumValueUsageInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxComplexEnumValueUsageInfo, SearchParameters>>("icu.windea.pls.search.complexEnumValueSearch")
        @JvmField
        val INSTANCE = ParadoxComplexEnumValueSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            enumName: String,
            selector: ChainedParadoxSelector<ParadoxComplexEnumValueUsageInfo>
        ): ParadoxQuery<ParadoxComplexEnumValueUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, enumName, selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            enumName: String,
            selector: ChainedParadoxSelector<ParadoxComplexEnumValueUsageInfo>
        ): ParadoxQuery<ParadoxComplexEnumValueUsageInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, enumName, selector))
        }
    }
}
