package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.index.ParadoxParameterIndexInfo

/**
 * 参数的查询。
 */
class ParadoxParameterSearch : ExtensibleQueryFactory<ParadoxParameterIndexInfo, ParadoxParameterSearch.Parameters>(EP_NAME) {
    /**
     * 参数的查询参数。
     *
     * @property name 参数的名字。
     * @property contextKey 上下文键（如 `scripted_trigger@x`、`inline_script@x`）。
     */
    data class Parameters(
        val name: String?,
        val contextKey: String,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxParameterIndexInfo>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxParameterIndexInfo>(project, context) {
        fun distinct() = distinctBy { it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxParameterIndexInfo, Parameters>>("icu.windea.pls.search.parameterSearch")
        @JvmField val INSTANCE = ParadoxParameterSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, contextKey: String, selector: Selector): ParadoxUnaryQuery<ParadoxParameterIndexInfo> {
            return INSTANCE.search(Parameters(name, contextKey, selector))
        }
    }
}
