package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举的查询。
 */
class ParadoxComplexEnumValueSearch : ExtensibleQueryFactory<ParadoxScriptStringExpressionElement, ParadoxComplexEnumValueSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 名字。
	 * @property enumName 枚举的名字。
	 */
	class SearchParameters(
		val name: String?,
		val enumName: String,
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxScriptStringExpressionElement>
	) : ParadoxSearchParameters<ParadoxScriptStringExpressionElement>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptStringExpressionElement, SearchParameters>>("icu.windea.pls.paradoxComplexEnumValueSearch")
		@JvmField val INSTANCE = ParadoxComplexEnumValueSearch()
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxComplexEnumValueSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			enumName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptStringExpressionElement> = nopSelector()
		): ParadoxQuery<ParadoxScriptStringExpressionElement, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, enumName, project, scope, selector))
		}
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxComplexEnumValueSearch.SearchParameters
		 */
		@JvmStatic
		fun searchAll(
			enumName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptStringExpressionElement> = nopSelector()
		): ParadoxQuery<ParadoxScriptStringExpressionElement, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, enumName, project, scope, selector))
		}
	}
}