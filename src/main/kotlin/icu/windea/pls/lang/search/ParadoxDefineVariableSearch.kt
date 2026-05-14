package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxDefineVariableSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值变量的查询。
 *
 * @see ParadoxDefineVariableSearcher
 */
class ParadoxDefineVariableSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefineVariableSearch.Parameters>(EP_NAME) {
    /**
     * 定值变量的查询参数。
     *
     * @property namespace 命名空间。
     * @property variable 变量名。
     */
    data class Parameters(
        val namespace: String?,
        val variable: String?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxScriptProperty> {
        fun createContext(scope: GlobalSearchScope = this.scope) = Context(namespace, variable, gameType, project, scope)
    }

    data class Context(
        val namespace: String?,
        val variable: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxScriptProperty>(project, context) {
        fun distinct() = distinctBy { ParadoxDefineManager.getExpression(it) }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.defineVariableSearch")
        @JvmField val INSTANCE = ParadoxDefineVariableSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(namespace: String?, variable: String?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(namespace, variable, selector))
        }
    }
}
