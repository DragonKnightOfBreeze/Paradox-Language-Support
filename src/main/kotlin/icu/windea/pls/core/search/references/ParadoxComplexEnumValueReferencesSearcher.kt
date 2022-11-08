package icu.windea.pls.core.search.references

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

/**
 * 复杂枚举值的使用的查询。
 *
 * 复杂枚举值的名字对应的PsiElement可能存在其他类型的使用（比如本地化），因此需要特殊处理。
 */
class ParadoxComplexEnumValueReferencesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxExpressionAwareElement) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val complexEnumValueInfo = target.complexEnumValueInfo ?: return@runReadActionInSmartMode
			queryParameters.optimizer.searchWord(complexEnumValueInfo.name, target.useScope, true, target)
		}
	}
}
