package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 同步本地化的查询器。
 */
class ParadoxSyncedLocalisationSearcher: QueryExecutorBase<ParadoxLocalisationProperty, ParadoxSyncedLocalisationSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxSyncedLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
		val project = queryParameters.project
		val scope = queryParameters.selector.scope
		if(queryParameters.name == null) {
			ParadoxSyncedLocalisationNameIndex.processAllElementsByKeys(project, scope) { _, it ->
				consumer.process(it)
			}
			return
		}
		ParadoxSyncedLocalisationNameIndex.processAllElements(queryParameters.name, project, scope) {
			consumer.process(it)
		}
	}
}