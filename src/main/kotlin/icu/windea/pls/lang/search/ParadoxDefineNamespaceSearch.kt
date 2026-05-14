package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxDefineNamespaceSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间的查询。
 */
class ParadoxDefineNamespaceSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefineNamespaceSearch.Parameters>(EP_NAME) {
    /**
     * 定值命名空间的查询参数。
     *
     * @property namespace 命名空间。
     */
    data class Parameters(
        val namespace: String?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    class Selector(project: Project, context: Any? = null) : ParadoxSearchSelector<ParadoxScriptProperty>(project, context) {
        fun distinct() = distinctBy { ParadoxDefineManager.getExpression(it) }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.defineNamespaceSearch")
        @JvmField val INSTANCE = ParadoxDefineNamespaceSearch()

        /**
         *  @see ParadoxDefineNamespaceSearch.Parameters
         */
        @JvmStatic
        fun search(namespace: String?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(namespace, selector))
        }
    }
}
