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
     * @property targetKey 目标键，包括目标定义的名字和类型信息（如 `type@name`）。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val mode: String?,
        val targetKey: String?,
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
            targetKey: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionInjectionIndexInfo, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(mode, targetKey, selector))
        }
    }
}
