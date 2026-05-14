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
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo

/**
 * 本地化参数的查询。
 */
class ParadoxLocalisationParameterSearch : ExtensibleQueryFactory<ParadoxLocalisationParameterIndexInfo, ParadoxLocalisationParameterSearch.Parameters>(EP_NAME) {
    /**
     * 本地化参数的查询参数。
     *
     * @property name 参数的名字。
     * @property localisationName 所属的本地化的名字。
     */
    data class Parameters(
        val name: String?,
        val localisationName: String,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxLocalisationParameterIndexInfo>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxLocalisationParameterIndexInfo>(project, context) {
        fun distinct() = distinctBy { it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxLocalisationParameterIndexInfo, Parameters>>("icu.windea.pls.search.localisationParameterSearch")
        @JvmField val INSTANCE = ParadoxLocalisationParameterSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, localisationName: String, selector: Selector): ParadoxUnaryQuery<ParadoxLocalisationParameterIndexInfo> {
            return INSTANCE.search(Parameters(name, localisationName, selector))
        }
    }
}
