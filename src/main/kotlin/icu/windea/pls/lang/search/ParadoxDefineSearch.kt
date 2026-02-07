package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.splitToPair
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 预定义的命名空间与变量的查询。
 */
class ParadoxDefineSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefineSearch.SearchParameters>(EP_NAME) {
    /**
     * @property namespace 命名空间。
     * @property variable 变量名。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val namespace: String?,
        val variable: String?,
        override val selector: ParadoxSearchSelector<ParadoxScriptProperty>
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, SearchParameters>>("icu.windea.pls.search.defineSearch")
        @JvmField
        val INSTANCE = ParadoxDefineSearch()

        /**
         *  @see ParadoxDefineSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            namespace: String?,
            variable: String?,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>
        ): ParadoxQuery<ParadoxScriptProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(namespace, variable, selector))
        }

        /**
         * @param expression 以点分隔的命名空间与变量名。如，`NAMESPACE.Variable`。
         */
        fun search(
            expression: String,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>
        ): ParadoxQuery<ParadoxScriptProperty, SearchParameters> {
            val (namespace, variable) = expression.splitToPair('.') ?: tupleOf(expression, null)
            return INSTANCE.createParadoxQuery(SearchParameters(namespace, variable, selector))
        }
    }
}
