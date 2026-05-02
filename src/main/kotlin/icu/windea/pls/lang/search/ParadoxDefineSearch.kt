package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.splitToPair
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间与变量的查询。
 */
class ParadoxDefineSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefineSearch.Parameters>(EP_NAME) {
    /**
     * 定值命名空间与变量的查询参数。
     *
     * @property namespace 命名空间。
     * @property variable 变量名。如果为空字符串，则表示查询命名空间。
     */
    class Parameters(
        val namespace: String?,
        val variable: String?,
        override val selector: ParadoxSearchSelector<ParadoxScriptProperty>
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, Parameters>>("icu.windea.pls.search.defineSearch")
        @JvmField val INSTANCE = ParadoxDefineSearch()

        /**
         *  @see ParadoxDefineSearch.Parameters
         */
        @JvmStatic
        fun search(
            namespace: String?,
            variable: String?,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>,
        ): ParadoxUnaryQuery<ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(namespace, variable, selector))
        }

        /**
         * @param expression 以点分隔的命名空间与变量名。如，`NAMESPACE.Variable`。
         */
        fun search(
            expression: String,
            selector: ParadoxSearchSelector<ParadoxScriptProperty>,
        ): ParadoxUnaryQuery<ParadoxScriptProperty> {
            val (namespace, variable) = expression.splitToPair('.') ?: tupleOf(expression, null)
            return INSTANCE.search(Parameters(namespace, variable, selector))
        }
    }
}
