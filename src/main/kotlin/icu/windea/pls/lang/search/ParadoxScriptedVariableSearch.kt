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
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.lang.search.searchers.ParadoxScriptedVariableSearcher

/**
 * 封装变量的查询。
 *
 * @see ParadoxScriptedVariableSearcher
 */
class ParadoxScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxScriptedVariableSearch.Parameters>(EP_NAME) {
    /**
     * 封装变量的查询参数。
     *
     * @property name 封装变量的名字（不以 `@` 开始）。
     * @property type 封装变量的类型（所有/本地/全局）。
     */
    data class Parameters(
        val name: String?,
        val type: ParadoxScriptedVariableType?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxScriptScriptedVariable>(project, context) {
        fun distinct() = distinctBy { it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptScriptedVariable, Parameters>>("icu.windea.pls.search.scriptedVariableSearch")
        @JvmField val INSTANCE = ParadoxScriptedVariableSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, type: ParadoxScriptedVariableType?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return INSTANCE.search(Parameters(name, type, selector))
        }

        /** @see Parameters */
        @JvmStatic
        fun searchLocal(name: String?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return search(name, ParadoxScriptedVariableType.Local, selector)
        }

        /** @see Parameters */
        @JvmStatic
        fun searchGlobal(name: String?, selector: Selector): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return search(name, ParadoxScriptedVariableType.Global, selector)
        }
    }
}
