package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
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
		
		/**
		 * 基于本地化名字索引，根据关键字和推断的语言区域遍历所有的本地化（localisation），并按照本地化的键进行去重。
		 * @see preferredParadoxLocale
		 */
		@JvmStatic
		fun processVariants(
			project: Project,
			scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector(),
			processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
		): Boolean {
			//如果索引未完成
			if(DumbService.isDumb(project)) return true
			
			//保证返回结果的名字的唯一性
			return ParadoxLocalisationNameIndex.processFirstElementByKeys(project, scope,
				predicate = { element -> selector.selectAll(element) },
				getDefaultValue = { selector.defaultValue },
				resetDefaultValue = { selector.defaultValue = null },
				processor = processor
			)
		}
	}
}

