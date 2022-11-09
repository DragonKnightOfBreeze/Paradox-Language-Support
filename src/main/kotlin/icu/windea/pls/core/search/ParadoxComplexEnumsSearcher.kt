package icu.windea.pls.core.search

import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举的查询器。
 */
class ParadoxComplexEnumsSearcher : QueryExecutor<PsiElement, ParadoxComplexEnumsSearch.SearchParameters> {
	override fun execute(queryParameters: ParadoxComplexEnumsSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		return ParadoxComplexEnumIndex.processAllElements(queryParameters.enumName, project, scope) {
			if(queryParameters.name == null || matches(it, queryParameters.name)) {
				consumer.process(it)
			}
			true
		}
	}
	
	private fun matches(it: ParadoxScriptExpressionElement, valueName: String): Boolean {
		return getName(it) == valueName
	}
	
	private fun getName(it: ParadoxScriptExpressionElement): String? {
		val name = runCatching { it.stub }.getOrNull()?.complexEnumValueInfo?.name
			?: it.value
		return name.takeIfNotEmpty()
	}
}