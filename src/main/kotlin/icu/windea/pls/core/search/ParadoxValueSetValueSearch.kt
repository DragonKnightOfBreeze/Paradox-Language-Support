package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 值集值的查询。（不涉及CWT规则文件中预定义的值）
 */
class ParadoxValueSetValueSearch : ExtensibleQueryFactory<ParadoxScriptString, ParadoxValueSetValueSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 名字。
	 * @property valueSetName 值集的名字。
	 */
	class SearchParameters(
		val name: String?,
		val valueSetName: String,
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxScriptString>
	) : ParadoxSearchParameters<ParadoxScriptString>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptString, SearchParameters>>("icu.windea.pls.paradoxValueSetValuesSearch")
		@JvmField val INSTANCE = ParadoxValueSetValueSearch()
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			valueSetName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptString> = nopSelector()
		): ParadoxQuery<ParadoxScriptString, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, valueSetName, project, scope, selector))
		}
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxValueSetValueSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			valueSetName: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxScriptString> = nopSelector()
		): ParadoxQuery<ParadoxScriptString, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, valueSetName, project, scope, selector))
		}
	}
}
