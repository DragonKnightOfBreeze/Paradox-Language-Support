package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值变量的查询。
 */
class ParadoxDefineVariableSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefineVariableSearch.Parameters>(EP_NAME) {
    /**
     * 定值变量的查询参数。
     *
     * @property namespace 命名空间。
     * @property variable 变量名。
     */
    class Parameters(
        val namespace: String?,
        val variable: String?,
        override val selector: ParadoxSearchSelector<ParadoxScriptProperty>,
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.defineVariableSearch")
        @JvmField val INSTANCE = ParadoxDefineVariableSearch()

        /**
         *  @see ParadoxDefineVariableSearch.Parameters
         */
        @JvmStatic
        fun search(
            namespace: String?,
            variable: String?,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>,
        ): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(namespace, variable, selector))
        }
    }
}
