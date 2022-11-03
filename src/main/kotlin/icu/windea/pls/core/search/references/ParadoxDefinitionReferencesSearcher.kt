package icu.windea.pls.core.search.references

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
 * 定义对应的PsiElement的名字（rootKey）不一定是定义的名字（definitionName），因此需要特殊处理。
 */
class ParadoxDefinitionReferencesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxDefinitionProperty) return
		DumbService.getInstance(queryParameters.project).runReadActionInSmartMode {
			doPrecessQuery(target, queryParameters.optimizer)
		}
	}
	
	private fun doPrecessQuery(target: ParadoxDefinitionProperty, optimizer: SearchRequestCollector) {
		val definitionInfo = target.definitionInfo ?: return
		optimizer.searchWord(definitionInfo.name, target.useScope, true, target)
	}
}