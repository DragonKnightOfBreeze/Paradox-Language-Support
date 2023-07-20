package icu.windea.pls.core.search.usage

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.script.psi.*
import kotlin.experimental.*

class ParadoxParameterUsagesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		val target = queryParameters.elementToSearch
		if(target !is ParadoxParameterElement) return
		val name = target.name
		if(name.isEmpty()) return
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_COMMENTS
			val processor = TheProcessor(target)
			queryParameters.optimizer.searchWord(name, useScope, searchContext, true, target, processor)
		}
	}
	
	private class TheProcessor(target: PsiElement): FilteredRequestResultProcessor(target) {
		override fun acceptElement(element: PsiElement): Boolean {
			return element is ParadoxParameter || element is ParadoxConditionParameter || element is ParadoxScriptStringExpressionElement
		}
		
		override fun acceptReference(reference: PsiReference): Boolean {
			return reference.canResolveParameter()
		}
	}
}

