package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的使用的查询。
 *
 * 对于同一名字的封装变量，其查找使用的结果应当是一致的。
 */
class ParadoxScriptedVariableUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxScriptScriptedVariable) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val name = target.name
			val processor = ParadoxRequestResultProcessor(target)
			queryParameters.optimizer.searchWord(name, target.useScope, UsageSearchContext.IN_CODE, true, target, processor)
		}
	}
}
