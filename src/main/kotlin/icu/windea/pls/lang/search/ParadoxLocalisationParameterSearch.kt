package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
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
    class Parameters(
        val name: String?,
        val localisationName: String,
        override val selector: ParadoxSearchSelector<ParadoxLocalisationParameterIndexInfo>
    ) : ParadoxSearchParameters<ParadoxLocalisationParameterIndexInfo>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxLocalisationParameterIndexInfo, Parameters>>("icu.windea.pls.search.localisationParameterSearch")
        @JvmField val INSTANCE = ParadoxLocalisationParameterSearch()

        /**
         * @see ParadoxLocalisationParameterSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            localisationName: String,
            selector: ParadoxSearchSelector<ParadoxLocalisationParameterIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxLocalisationParameterIndexInfo> {
            return INSTANCE.search(Parameters(name, localisationName, selector))
        }
    }
}
