package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.chained.*
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
		 * 
		 * 优化代码提示时会用到此方法。
		 */
		@JvmStatic
		fun processVariants(
			keyword: String,
			selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
			processor: (ParadoxLocalisationProperty) -> Boolean
		): Boolean {
			//保证返回结果的名字的唯一性
			val project = selector.project
			val scope = selector.scope
			return ParadoxLocalisationNameIndex.KEY.processFirstElementByKeys(project, scope,
				keyPredicate = { key -> key.matchesKeyword(keyword) },
				predicate = { element -> selector.select(element) },
				getDefaultValue = { selector.defaultValue },
				resetDefaultValue = { selector.defaultValue = null },
				processor = processor
			)
		}
	}
}

