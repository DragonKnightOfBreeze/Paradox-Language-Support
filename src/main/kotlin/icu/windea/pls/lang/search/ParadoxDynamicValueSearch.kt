package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo

/**
 * 动态值的查询。（不涉及CWT规则文件中预定义的值）
 */
class ParadoxDynamicValueSearch : ExtensibleQueryFactory<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 名字。
     * @property dynamicValueTypes 值集的名字。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val name: String?,
        val dynamicValueTypes: Set<String>,
        override val selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>
    ) : ParadoxSearchParameters<ParadoxDynamicValueIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDynamicValueIndexInfo, SearchParameters>>("icu.windea.pls.search.dynamicValueSearch")
        @JvmField
        val INSTANCE = ParadoxDynamicValueSearch()

        /**
         * @see ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            dynamicValueType: String,
            selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, setOf(dynamicValueType), selector))
        }

        /**
         * @see ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            dynamicValueTypes: Set<String>,
            selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, dynamicValueTypes, selector))
        }
    }
}
