package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询。（本地：同一脚本文件）
 */
class ParadoxLocalScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 变量的名字，不以"@"开始。
     */
    class SearchParameters(
        val name: String?,
        override val selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
    ) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptScriptedVariable, SearchParameters>>("icu.windea.pls.search.localScriptedVariableSearch")
        @JvmField val INSTANCE = ParadoxLocalScriptedVariableSearch()
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxLocalScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String,
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
        }
        
        /**
         *  @see icu.windea.pls.core.search.ParadoxLocalScriptedVariableSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
        ): ParadoxQuery<ParadoxScriptScriptedVariable, SearchParameters> {
            return INSTANCE.createParadoxQuery(SearchParameters(null, selector))
        }
    }
}