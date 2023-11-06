package icu.windea.pls.core.search.usage

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import kotlin.experimental.*

/**
 * 本地化的使用的查询。
 */
class ParadoxLocalisationUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxLocalisationProperty) return
		val name = target.name
		if(name.isEmpty()) return
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_COMMENTS
			val processor = getProcessor(target)
			queryParameters.optimizer.wordRequests.removeIf { it.word == name }
			queryParameters.optimizer.searchWord(name, useScope, searchContext, true, target, processor)
		}
	}
	
	private fun getProcessor(target: PsiElement): RequestResultProcessor {
		return ParadoxFilteredRequestResultProcessor(target, ParadoxResolveConstraint.Localisation)
	}
}

