package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo

/**
 * 动态值的查询。
 *
 * 不涉及规则文件中预定义的值。
 */
class ParadoxDynamicValueSearch : ExtensibleQueryFactory<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.Parameters>(EP_NAME) {
    /**
     * 动态值的查询参数。
     *
     * @property name 动态值的名字。
     * @property types 动态值的类型。
     */
    class Parameters(
        val name: String?,
        val types: Set<String>,
        override val selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>
    ) : ParadoxSearchParameters<ParadoxDynamicValueIndexInfo>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDynamicValueIndexInfo, Parameters>>("icu.windea.pls.search.dynamicValueSearch")
        @JvmField val INSTANCE = ParadoxDynamicValueSearch()

        /**
         * @see ParadoxDynamicValueSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: String,
            selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxDynamicValueIndexInfo> {
            return INSTANCE.search(Parameters(name, setOf(type), selector))
        }

        /**
         * @see ParadoxDynamicValueSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            types: Set<String>,
            selector: ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxDynamicValueIndexInfo> {
            return INSTANCE.search(Parameters(name, types, selector))
        }
    }
}
