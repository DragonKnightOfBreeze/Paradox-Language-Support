package icu.windea.pls.core.search

import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 值集中的值的查询器。
 */
class ParadoxValueSetValuesSearcher : QueryExecutor<PsiElement, ParadoxValueSetValuesSearch.SearchParameters> {
	override fun execute(queryParameters: ParadoxValueSetValuesSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		return ParadoxValueSetValueIndex.processAllElements(queryParameters.valueSetName, project, scope) {
			if(queryParameters.name == null || matches(it, queryParameters.name)) {
				consumer.process(it)
			}
			true
		}
	}
	
	private fun matches(it: ParadoxScriptString, valueName: String): Boolean {
		return getName(it) == valueName
	}
	
	private fun getName(it: ParadoxScriptString): String? {
		val name = runCatching { it.stub }.getOrNull()?.valueSetValueInfo?.name
			?: it.value.substringBefore('@')
		return name.takeIfNotEmpty()
	}
}

