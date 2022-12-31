package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的查询器。
 */
class ParadoxLocalisationSearcher: QueryExecutorBase<ParadoxLocalisationProperty, ParadoxLocalisationSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxLocalisationSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationProperty>) {
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		if(queryParameters.name == null) {
			ParadoxLocalisationNameIndex.processAllElementsByKeys(project, scope) { _, it ->
				consumer.process(it)
			}
			return
		}
		ParadoxLocalisationNameIndex.processAllElements(queryParameters.name, project, scope) {
			consumer.process(it)
		}
	}
}
