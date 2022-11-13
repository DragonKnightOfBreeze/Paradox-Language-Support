package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询。
 */
class ParadoxDefinitionSearch: ExtensibleQueryFactory<ParadoxDefinitionProperty, ParadoxDefinitionSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 定义的名字。
	 * @property typeExpression 定义的类型表达式。示例：`event` `civic_or_origin.civic` `sprite|spriteType`
	 */
	class SearchParameters(
		val name: String?,
		val typeExpression: String?,
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxDefinitionProperty>
	) : ParadoxSearchParameters<ParadoxDefinitionProperty>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxDefinitionProperty, SearchParameters>>("icu.windea.pls.paradoxDefinitionSearch")
		@JvmField val INSTANCE = ParadoxDefinitionSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxDefinitionSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			typeExpression: String?,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(name, typeExpression, project, scope, selector))
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxDefinitionSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			typeExpression: String?,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(null, typeExpression, project, scope, selector))
	}
}