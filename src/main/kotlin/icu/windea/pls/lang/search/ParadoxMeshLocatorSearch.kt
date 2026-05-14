package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo

/**
 * 网格定位器（mesh locator）的查询。
 */
class ParadoxMeshLocatorSearch : ExtensibleQueryFactory<ParadoxMeshLocatorIndexInfo, ParadoxMeshLocatorSearch.Parameters>(EP_NAME) {
    /**
     * 网格定位器（mesh locator）的查询参数。
     *
     * @property name 名字。
     */
    data class Parameters(
        val name: String?,
        override val selector: ParadoxSearchSelector<ParadoxMeshLocatorIndexInfo>
    ) : ParadoxSearchParameters<ParadoxMeshLocatorIndexInfo> {
        fun createContext(scope: GlobalSearchScope = this.scope) = Context(name, gameType, project, scope)
    }

    data class Context(
        val name: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxMeshLocatorIndexInfo, Parameters>>("icu.windea.pls.search.meshLocatorSearch")
        @JvmField val INSTANCE = ParadoxMeshLocatorSearch()

        @JvmStatic
        fun selector(project: Project, context: Any? = null) = ParadoxSearchSelector<ParadoxMeshLocatorIndexInfo>(project, context)

        /**
         * @see Parameters
         */
        @JvmStatic
        fun search(name: String?, selector: ParadoxSearchSelector<ParadoxMeshLocatorIndexInfo>): ParadoxUnaryQuery<ParadoxMeshLocatorIndexInfo> {
            return INSTANCE.search(Parameters(name, selector))
        }
    }
}
