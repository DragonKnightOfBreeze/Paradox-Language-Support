package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selectors.chained.*
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
		override val selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
	) : ParadoxSearchParameters<ParadoxLocalisationProperty>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationProperty, SearchParameters>>("icu.windea.pls.paradoxLocalisationSearch")
		@JvmField val INSTANCE = ParadoxLocalisationSearch()
		
		//FIXME 指定名字的查询最慢可能需要400+ms
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalisationSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
		): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, selector))
		}
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalisationSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
		): ParadoxQuery<ParadoxLocalisationProperty, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, selector))
		}
		
		/**
		 * 基于本地化名字索引，根据关键字和推断的语言区域遍历所有的本地化（localisation），并按照本地化的键进行去重。
		 */
		@JvmStatic
		fun processVariants(
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
			processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
		): Boolean {
			//如果索引未完成
			val project = selector.project
			if(DumbService.isDumb(project)) return true
			
			//保证返回结果的名字的唯一性
			val scope = selector.scope
			return ParadoxLocalisationNameIndex.KEY.processFirstElementByKeys(project, scope,
				predicate = { element -> selector.selectAll(element) },
				getDefaultValue = { selector.defaultValue },
				resetDefaultValue = { selector.defaultValue = null },
				processor = processor
			)
		}
	}
}

