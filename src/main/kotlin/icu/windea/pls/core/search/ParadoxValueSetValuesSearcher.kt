package icu.windea.pls.core.search

import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

/**
 * 值集中的值的查询器。
 */
class ParadoxValueSetValuesSearcher : QueryExecutor<PsiElement, ParadoxValueSetValuesSearch.SearchParameters> {
	override fun execute(queryParameters: ParadoxValueSetValuesSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		val name = queryParameters.name
		val valueSetName = queryParameters.valueSetName
		return ParadoxValueSetValueIndex.processAllElements(valueSetName, project, scope) {
			if(name == null || matches(it, name)) {
				consumer.process(it)
			}
			true
		}
	}
	
	private fun matches(element: ParadoxScriptString, name: String): Boolean {
		return ParadoxValueSetValueInfoHandler.resolveName(element) == name
	}
}

