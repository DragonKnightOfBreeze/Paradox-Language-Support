package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.indexInfo.*

/**
 * 动态值的查询。（不涉及CWT规则文件中预定义的值）
 */
class ParadoxDynamicValueSearch : ExtensibleQueryFactory<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 名字。
     * @property dynamicValueTypes 值集的名字。
     */
    class SearchParameters(
        val name: String?,
        val dynamicValueTypes: Set<String>,
        override val selector: ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>
    ) : ParadoxSearchParameters<ParadoxDynamicValueIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxDynamicValueIndexInfo, SearchParameters>>("icu.windea.pls.search.dynamicValueSearch")
        @JvmField
        val INSTANCE = ParadoxDynamicValueSearch()

        /**
         * @see icu.windea.pls.lang.search.ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            dynamicValueType: String,
            selector: ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, setOf(dynamicValueType), selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            dynamicValueTypes: Set<String>,
            selector: ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, dynamicValueTypes, selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            dynamicValueType: String,
            selector: ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, setOf(dynamicValueType), selector))
        }

        /**
         * @see icu.windea.pls.lang.search.ParadoxDynamicValueSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            dynamicValueTypes: Set<String>,
            selector: ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>
        ): ParadoxQuery<ParadoxDynamicValueIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, dynamicValueTypes, selector))
        }
    }
}
