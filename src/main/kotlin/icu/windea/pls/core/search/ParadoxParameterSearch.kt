package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.model.expression.*

class ParadoxParameterSearch: ExtensibleQueryFactory<ParadoxParameterInfo, ParadoxParameterSearch.SearchParameters>(EP_NAME) {
	class SearchParameters(
		val name: String?,
		val contextKey: String,
		override val selector: ChainedParadoxSelector<ParadoxParameterInfo>
	) : ParadoxSearchParameters<ParadoxParameterInfo>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxParameterInfo, SearchParameters>>("icu.windea.pls.search.parameterSearch")
		@JvmField val INSTANCE = ParadoxParameterSearch()
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxParameterSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			contextKey: String,
			selector: ChainedParadoxSelector<ParadoxParameterInfo>
		): ParadoxQuery<ParadoxParameterInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, contextKey, selector))
		}
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxParameterSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			contextKey: String,
			selector: ChainedParadoxSelector<ParadoxParameterInfo>
		): ParadoxQuery<ParadoxParameterInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, contextKey, selector))
		}
	}
}