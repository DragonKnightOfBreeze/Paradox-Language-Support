package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.model.expressionInfo.*

class ParadoxLocalisationParameterSearch: ExtensibleQueryFactory<ParadoxLocalisationParameterInfo, ParadoxLocalisationParameterSearch.SearchParameters>(EP_NAME) {
	class SearchParameters(
		val name: String?,
		val localisationName: String,
		override val selector: ChainedParadoxSelector<ParadoxLocalisationParameterInfo>
	) : ParadoxSearchParameters<ParadoxLocalisationParameterInfo>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxLocalisationParameterInfo, SearchParameters>>("icu.windea.pls.search.localisationParameterSearch")
		@JvmField val INSTANCE = ParadoxLocalisationParameterSearch()
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxLocalisationParameterSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			localisationName: String,
			selector: ChainedParadoxSelector<ParadoxLocalisationParameterInfo>
		): ParadoxQuery<ParadoxLocalisationParameterInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(name, localisationName, selector))
		}
		
		/**
		 * @see icu.windea.pls.core.search.ParadoxLocalisationParameterSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			localisationName: String,
			selector: ChainedParadoxSelector<ParadoxLocalisationParameterInfo>
		): ParadoxQuery<ParadoxLocalisationParameterInfo, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, localisationName, selector))
		}
	}
}
