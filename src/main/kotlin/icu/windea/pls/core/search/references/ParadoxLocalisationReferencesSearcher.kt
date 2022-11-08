package icu.windea.pls.core.search.references

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的使用的查询。
 *
 * 对于同一名字和类型（localisation/synced_localisation）的本地化，其查找使用的结果应当是一致的。
 */
class ParadoxLocalisationReferencesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxLocalisationProperty) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val name = target.name
			queryParameters.optimizer.searchWord(name, target.useScope, true, target)
		}
	}
}
