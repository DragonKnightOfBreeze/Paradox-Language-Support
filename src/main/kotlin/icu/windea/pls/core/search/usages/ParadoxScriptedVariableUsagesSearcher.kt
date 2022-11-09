package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的使用的查询。
 */
class ParadoxScriptedVariableUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxScriptScriptedVariable) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val name = target.name
			val useScope = getUseScope(queryParameters)
			queryParameters.optimizer.searchWord(name, useScope, true, target)
		}
	}
}
