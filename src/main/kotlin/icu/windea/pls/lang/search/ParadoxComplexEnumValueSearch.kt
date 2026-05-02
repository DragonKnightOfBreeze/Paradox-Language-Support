package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo

/**
 * 复杂枚举的查询。
 */
class ParadoxComplexEnumValueSearch : ExtensibleQueryFactory<ParadoxComplexEnumValueIndexInfo, ParadoxComplexEnumValueSearch.Parameters>(EP_NAME) {
    /**
     * 复杂枚举的查询参数。
     *
     * @property name 名字。
     * @property enumName 枚举的名字。
     */
    class Parameters(
        val name: String?,
        val enumName: String,
        override val selector: ParadoxSearchSelector<ParadoxComplexEnumValueIndexInfo>
    ) : ParadoxSearchParameters<ParadoxComplexEnumValueIndexInfo>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxComplexEnumValueIndexInfo, Parameters>>("icu.windea.pls.search.complexEnumValueSearch")
        @JvmField val INSTANCE = ParadoxComplexEnumValueSearch()

        /**
         * @see ParadoxComplexEnumValueSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            enumName: String,
            selector: ParadoxSearchSelector<ParadoxComplexEnumValueIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxComplexEnumValueIndexInfo> {
            return INSTANCE.search(Parameters(name, enumName, selector))
        }
    }
}
