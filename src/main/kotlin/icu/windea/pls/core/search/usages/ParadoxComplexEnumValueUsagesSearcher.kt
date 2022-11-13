package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举值的使用的查询。
 *
 * 复杂枚举值的名字对应的PsiElement可能存在其他类型的使用（比如本地化），因此需要特殊处理。
 */
class ParadoxComplexEnumValueUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxScriptExpressionElement) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			val complexEnumValueInfo = target.complexEnumValueInfo ?: return@runReadActionInSmartMode
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			var useScope = queryParameters.effectiveSearchScope
			//需要特别处理指定了searchScope的情况
			val complexEnumConfig = complexEnumValueInfo.getConfig(queryParameters.project)
			if(complexEnumConfig?.searchScope != null) {
				val globalSearchScope = ParadoxSearchScope(complexEnumConfig.searchScope).getGlobalSearchScope(target)
				if(globalSearchScope != null){
					useScope = useScope.intersectWith(globalSearchScope)
				}
			}
			queryParameters.optimizer.searchWord(complexEnumValueInfo.name, useScope, true, target)
		}
	}
}
