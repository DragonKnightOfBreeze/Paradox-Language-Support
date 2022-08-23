package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 值集中的值的查询器。
 */
class ParadoxValueSetValuesSearch : ExtensibleQueryFactory<ParadoxScriptExpressionElement, ParadoxValueSetValuesSearch.SearchParameters>(EP_NAME) {
	class SearchParameters(
		val name: String?,
		val valueSetName: String,
		override val project: Project,
		override val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxScriptExpressionElement>
	) : ParadoxSearchParameters<ParadoxScriptExpressionElement>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptExpressionElement, SearchParameters>>("icu.windea.pls.paradoxValueSetValuesSearch")
		@JvmField val INSTANCE = ParadoxValueSetValuesSearch()
		
		fun search(
			name: String,
			valueSetName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptExpressionElement> = nopSelector()
		) = INSTANCE.createParadoxResultsQuery(SearchParameters(name, valueSetName, project, scope, selector))
		
		fun search(
			valueSetName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptExpressionElement> = nopSelector()
		) = INSTANCE.createParadoxResultsQuery(SearchParameters(null, valueSetName, project, scope, selector))
	}
}