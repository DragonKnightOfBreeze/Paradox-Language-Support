package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 定义的使用的查询。
 *
 * 对于同一名字和类型的定义，其查找使用的结果应当是一致的。
 *
 * 定义对应的PsiElement的名字（rootKey）不一定是定义的名字（definitionName），因此需要特殊处理。
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxDefinitionProperty) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val definitionInfo = target.definitionInfo ?: return@runReadActionInSmartMode
			val name = definitionInfo.name
			val processor = ParadoxRequestResultProcessor(target)
			queryParameters.optimizer.searchWord(name, target.useScope, UsageSearchContext.IN_CODE, true, target, processor)
		}
	}
}
