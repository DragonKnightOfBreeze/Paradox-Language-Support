package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义注入的查询。
 */
class ParadoxDefinitionInjectionSearch : ExtensibleQueryFactory<ParadoxScriptProperty, ParadoxDefinitionInjectionSearch.SearchParameters>(EP_NAME) {
    /**
     * @property mode 注入模式。
     * @property target 目标定义的名字。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val mode: String?,
        val target: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptProperty>,
    ) : ParadoxSearchParameters<ParadoxScriptProperty>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptProperty, SearchParameters>>("icu.windea.pls.search.definitionInjectionSearch")
        @JvmField
        val INSTANCE = ParadoxDefinitionInjectionSearch()

        /**
         * @see ParadoxDefinitionInjectionSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            mode: String?,
            target: String?,
            selector: ChainedParadoxSelector<ParadoxScriptProperty>,
        ): ParadoxQuery<ParadoxScriptProperty, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(mode, target, selector))
        }
    }
}
