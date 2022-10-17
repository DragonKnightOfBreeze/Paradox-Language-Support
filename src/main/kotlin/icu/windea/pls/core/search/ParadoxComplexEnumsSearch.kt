package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举的查询。
 */
class ParadoxComplexEnumsSearch : ExtensibleQueryFactory<ParadoxScriptExpressionElement, ParadoxComplexEnumsSearch.SearchParameters>(EP_NAME) {
	class SearchParameters(
		val name: String?,
		val enumName: String,
		override val project: Project,
		override val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxScriptExpressionElement>
	) : ParadoxSearchParameters<ParadoxScriptExpressionElement>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptExpressionElement, SearchParameters>>("icu.windea.pls.paradoxComplexEnumsSearch")
		@JvmField val INSTANCE = ParadoxComplexEnumsSearch()
		
		fun search(
			name: String,
			enumName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptExpressionElement> = nopSelector()
		) = INSTANCE.createParadoxResultsQuery(SearchParameters(name, enumName, project, scope, selector))
		
		fun search(
			enumName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptExpressionElement> = nopSelector()
		) = INSTANCE.createParadoxResultsQuery(SearchParameters(null, enumName, project, scope, selector))
	}
}