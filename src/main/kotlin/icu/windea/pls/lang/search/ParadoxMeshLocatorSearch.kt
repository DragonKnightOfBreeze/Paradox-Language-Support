package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxMeshLocatorSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.createParadoxQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo

/**
 * 网格定位器（mesh locator）的查询。
 *
 * @see ParadoxMeshLocatorSearcher
 */
class ParadoxMeshLocatorSearch : ExtensibleQueryFactory<ParadoxMeshLocatorIndexInfo, ParadoxMeshLocatorSearch.Parameters>(EP_NAME) {
    /**
     * 网格定位器（mesh locator）的查询参数。
     *
     * @property name 名字。
     */
    data class Parameters(
        val name: String?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxMeshLocatorIndexInfo>


    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxMeshLocatorIndexInfo>(project, context) {
        fun distinct() = distinctBy { it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxMeshLocatorIndexInfo, Parameters>>("icu.windea.pls.search.meshLocatorSearch")
        @JvmField val INSTANCE = ParadoxMeshLocatorSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, selector: Selector): ParadoxUnaryQuery<ParadoxMeshLocatorIndexInfo> {
            return INSTANCE.createParadoxQuery(Parameters(name, selector))
        }
    }
}
