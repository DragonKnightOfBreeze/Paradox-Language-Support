package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxComplexEnumValueSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo

/**
 * 复杂枚举的查询。
 *
 * @see ParadoxComplexEnumValueSearcher
 */
class ParadoxComplexEnumValueSearch : ExtensibleQueryFactory<ParadoxComplexEnumValueIndexInfo, ParadoxComplexEnumValueSearch.Parameters>(EP_NAME) {
    /**
     * 复杂枚举的查询参数。
     *
     * @property name 名字。
     * @property enumName 枚举的名字。
     */
    data class Parameters(
        val name: String?,
        val enumName: String,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxComplexEnumValueIndexInfo>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxComplexEnumValueIndexInfo>(project, context) {
        fun distinct() = distinctBy { if (it.caseInsensitive) it.name.lowercase() else it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxComplexEnumValueIndexInfo, Parameters>>("icu.windea.pls.search.complexEnumValueSearch")
        @JvmField val INSTANCE = ParadoxComplexEnumValueSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, enumName: String, selector: Selector): ParadoxUnaryQuery<ParadoxComplexEnumValueIndexInfo> {
            return INSTANCE.search(Parameters(name, enumName, selector))
        }
    }
}
