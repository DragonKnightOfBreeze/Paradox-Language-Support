package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxScriptStringExpressionElement, ParadoxComplexEnumValueSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, consumer: Processor<in ParadoxScriptStringExpressionElement>) {
		val name = queryParameters.name
		val enumName = queryParameters.enumName
		val project = queryParameters.project
		val scope = queryParameters.selector.getGlobalSearchScope() ?: GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		ParadoxComplexEnumIndex.processAllElements(enumName, project, scope) {
			if(name != null && !matchesName(it, name)) return@processAllElements true
			consumer.process(it)
		}
	}
	
	private fun matchesName(element: ParadoxScriptStringExpressionElement, name: String): Boolean {
		return ParadoxComplexEnumValueHandler.getName(element) == name
	}
}