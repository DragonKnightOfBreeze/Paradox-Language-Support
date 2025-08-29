package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.splitToPair
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo

/**
 * 预定义的命名空间与变量的查询。
 */
class ParadoxDefineSearch : ExtensibleQueryFactory<ParadoxDefineIndexInfo, ParadoxDefineSearch.SearchParameters>(EP_NAME) {
    /**
     * @property namespace 命名空间。
     * @property variable 变量名。
     */
    class SearchParameters(
        val namespace: String?,
        val variable: String?,
        override val selector: ChainedParadoxSelector<ParadoxDefineIndexInfo>
    ) : ParadoxSearchParameters<ParadoxDefineIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefineIndexInfo, SearchParameters>>("icu.windea.pls.search.defineSearch")
        @JvmField
        val INSTANCE = ParadoxDefineSearch()

        /**
         *  @see ParadoxDefineSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            namespace: String?,
            variable: String?,
            selector: ChainedParadoxSelector<ParadoxDefineIndexInfo>
        ): ParadoxQuery<ParadoxDefineIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(namespace, variable, selector))
        }

        /**
         * @param expression 以点分隔的命名空间与变量名。如，`NAMESPACE.Variable`。
         */
        fun search(
            expression: String,
            selector: ChainedParadoxSelector<ParadoxDefineIndexInfo>
        ): ParadoxQuery<ParadoxDefineIndexInfo, SearchParameters> {
            val (namespace, variable) = expression.splitToPair('.') ?: tupleOf(expression, null)
            return INSTANCE.createParadoxQuery(SearchParameters(namespace, variable, selector))
        }
    }
}
