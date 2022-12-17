package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.chained.*
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
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
	) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptScriptedVariable, SearchParameters>>("icu.windea.pls.paradoxGlobalScriptedVariableSearch")
		@JvmField val INSTANCE = ParadoxGlobalScriptedVariableSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxGlobalScriptedVariableSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(name, project, scope, selector))
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxGlobalScriptedVariableSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(null, project, scope, selector))
	}
}
