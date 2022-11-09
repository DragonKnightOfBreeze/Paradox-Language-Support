package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的使用的查询。
 */
class ParadoxLocalisationUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxLocalisationProperty) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val name = target.name
			val useScope = target.useScope
			queryParameters.optimizer.searchWord(name, useScope, true, target)
		}
	}
}
