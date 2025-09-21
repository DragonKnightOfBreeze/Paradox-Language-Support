package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 定义的查询。
 */
class ParadoxDefinitionSearch : ExtensibleQueryFactory<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 定义的名字。
     * @property typeExpression 定义的类型表达式。
     * @property selector 查询选择器。
     * @property forFile 是否查询作为文件的定义。默认为 `true`。
     */
    class SearchParameters(
        val name: String?,
        val typeExpression: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>,
        val forFile: Boolean = true,
    ) : ParadoxSearchParameters<ParadoxScriptDefinitionElement>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxScriptDefinitionElement, SearchParameters>>("icu.windea.pls.search.definitionSearch")
        @JvmField
        val INSTANCE = ParadoxDefinitionSearch()

        /** @see ParadoxDefinitionSearch.SearchParameters*/
        @JvmStatic
        fun search(
            name: String?,
            typeExpression: String?,
            selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>,
            forFile: Boolean = true,
        ): ParadoxQuery<ParadoxScriptDefinitionElement, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, typeExpression, selector, forFile))
        }
    }
}
