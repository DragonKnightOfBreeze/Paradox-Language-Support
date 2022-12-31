package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的查询。
 */
class ParadoxLocalisationSearch: ExtensibleQueryFactory<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 本地化的名字。
	 */
	class SearchParameters(
		val name: String?,
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
	) : ParadoxSearchParameters<ParadoxLocalisationProperty>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.paradoxLocalisationSearch")
		@JvmField val INSTANCE = ParadoxLocalisationSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalisationSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(name, project, scope, selector))
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalisationSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			project: Project,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(null, project, scope, selector))
	}
}

