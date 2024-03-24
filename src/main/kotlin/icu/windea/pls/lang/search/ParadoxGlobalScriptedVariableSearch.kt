package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 全局封装变量的查询。
 */
class ParadoxGlobalScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxGlobalScriptedVariableSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 变量的名字，不以"@"开始。
     */
    class SearchParameters(
        val name: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
    ) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptScriptedVariable, SearchParameters>>("icu.windea.pls.search.globalScriptedVariableSearch")
        @JvmField val INSTANCE = ParadoxGlobalScriptedVariableSearch()
        
        /**
         *  @see icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }
        
        /**
         *  @see icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, selector))
        }
    }
}
