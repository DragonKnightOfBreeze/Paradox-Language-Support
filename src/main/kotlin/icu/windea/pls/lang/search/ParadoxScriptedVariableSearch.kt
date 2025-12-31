package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的查询。
 */
class ParadoxScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxScriptedVariableSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 封装变量的名字（不以 `@` 开始）。
     * @property type 封装变量的类型（所有/本地/全局）。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val name: String?,
        val type: ParadoxScriptedVariableType? = null,
        override val selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
    ) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptScriptedVariable, SearchParameters>>("icu.windea.pls.search.scriptedVariableSearch")
        @JvmField
        val INSTANCE = ParadoxScriptedVariableSearch()

        /**
         * @see ParadoxScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: ParadoxScriptedVariableType?,
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, type, selector))
        }

        /**
         * @see ParadoxScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun searchLocal(
            name: String?,
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return search(name, ParadoxScriptedVariableType.Local, selector)
        }

        /**
         * @see ParadoxScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun searchGlobal(
            name: String?,
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return search(name, ParadoxScriptedVariableType.Global, selector)
        }
    }
}
