package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询。
 */
class ParadoxDefinitionSearch : ExtensibleQueryFactory<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 定义的名字。
     * @property typeExpression 定义的类型表达式。示例：`event` `civic_or_origin.civic` `sprite`
     */
    class SearchParameters(
        val name: String?,
        val typeExpression: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>
    ) : ParadoxSearchParameters<ParadoxScriptDefinitionElement>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptDefinitionElement, SearchParameters>>("icu.windea.pls.search.definitionSearch")
        @JvmField val INSTANCE = ParadoxDefinitionSearch()
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxDefinitionSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            typeExpression: String?,
            selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>
        ): ParadoxQuery<ParadoxScriptDefinitionElement, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, typeExpression, selector))
        }
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxDefinitionSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            typeExpression: String?,
            selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>
        ): ParadoxQuery<ParadoxScriptDefinitionElement, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, typeExpression, selector))
        }
    }
}

