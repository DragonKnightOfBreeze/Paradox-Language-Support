package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
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
    class Parameters(
        val name: String?,
        val contextKey: String,
        override val selector: ParadoxSearchSelector<ParadoxParameterIndexInfo>
    ) : ParadoxSearchParameters<ParadoxParameterIndexInfo>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxParameterIndexInfo, Parameters>>("icu.windea.pls.search.parameterSearch")
        @JvmField val INSTANCE = ParadoxParameterSearch()

        /**
         * @see ParadoxParameterSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            contextKey: String,
            selector: ParadoxSearchSelector<ParadoxParameterIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxParameterIndexInfo> {
            return INSTANCE.search(Parameters(name, contextKey, selector))
        }
    }
}
