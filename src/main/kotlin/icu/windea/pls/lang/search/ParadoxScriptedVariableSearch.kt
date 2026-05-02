package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的查询。
 */
class ParadoxScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxScriptedVariableSearch.Parameters>(EP_NAME) {
    /**
     * 封装变量的查询参数。
     *
     * @property name 封装变量的名字（不以 `@` 开始）。
     * @property type 封装变量的类型（所有/本地/全局）。
     */
    class Parameters(
        val name: String?,
        val type: ParadoxScriptedVariableType? = null,
        override val selector: ParadoxSearchSelector<ParadoxScriptScriptedVariable>
    ) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptScriptedVariable, Parameters>>("icu.windea.pls.search.scriptedVariableSearch")
        @JvmField val INSTANCE = ParadoxScriptedVariableSearch()

        /**
         * @see ParadoxScriptedVariableSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: ParadoxScriptedVariableType?,
            selector: ParadoxSearchSelector<ParadoxScriptScriptedVariable>,
        ): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return INSTANCE.search(Parameters(name, type, selector))
        }

        /**
         * @see ParadoxScriptedVariableSearch.Parameters
         */
        @JvmStatic
        fun searchLocal(
            name: String?,
            selector: ParadoxSearchSelector<ParadoxScriptScriptedVariable>,
        ): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return search(name, ParadoxScriptedVariableType.Local, selector)
        }

        /**
         * @see ParadoxScriptedVariableSearch.Parameters
         */
        @JvmStatic
        fun searchGlobal(
            name: String?,
            selector: ParadoxSearchSelector<ParadoxScriptScriptedVariable>,
        ): ParadoxUnaryQuery<ParadoxScriptScriptedVariable> {
            return search(name, ParadoxScriptedVariableType.Global, selector)
        }
    }
}
