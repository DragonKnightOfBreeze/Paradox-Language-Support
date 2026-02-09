package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo

/**
 * 定义注入的查询。
 */
class ParadoxDefinitionInjectionSearch : ExtensibleQueryFactory<ParadoxDefinitionInjectionIndexInfo, ParadoxDefinitionInjectionSearch.SearchParameters>(EP_NAME) {
    /**
     * @property mode 注入模式。
     * @property target 目标定义的名字。
     * @property type 目标定义的类型。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val mode: String?,
        val target: String?,
        val type: String?,
        override val selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
    ) : ParadoxSearchParameters<ParadoxDefinitionInjectionIndexInfo>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefinitionInjectionIndexInfo, SearchParameters>>("icu.windea.pls.search.definitionInjectionSearch")
        @JvmField
        val INSTANCE = ParadoxDefinitionInjectionSearch()

        /**
         * @see ParadoxDefinitionInjectionSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            mode: String?,
            target: String?,
            type: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionInjectionIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(mode, target, type, selector))
        }
    }
}
