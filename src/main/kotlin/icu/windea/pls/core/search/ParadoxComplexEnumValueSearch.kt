package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*

/**
 * 复杂枚举的查询。
 */
class ParadoxComplexEnumValueSearch : ExtensibleQueryFactory<ParadoxComplexEnumValueInfo, ParadoxComplexEnumValueSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 名字。
	 * @property enumName 枚举的名字。
	 */
	class SearchParameters(
		val name: String?,
		val enumName: String,
		override val selector: ChainedParadoxSelector<ParadoxComplexEnumValueInfo>
	) : ParadoxSearchParameters<ParadoxComplexEnumValueInfo>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxComplexEnumValueInfo, SearchParameters>>("icu.windea.pls.paradoxComplexEnumValueSearch")
		@JvmField val INSTANCE = ParadoxComplexEnumValueSearch()
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxComplexEnumValueSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			enumName: String,
			selector: ChainedParadoxSelector<ParadoxComplexEnumValueInfo>
		): ParadoxQuery<ParadoxComplexEnumValueInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, enumName, selector))
		}
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxComplexEnumValueSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			enumName: String,
			selector: ChainedParadoxSelector<ParadoxComplexEnumValueInfo>
		): ParadoxQuery<ParadoxComplexEnumValueInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, enumName, selector))
		}
	}
}