package icu.windea.pls.core.search.implementations

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		//得到解析后的PSI元素
		val sourceElement = queryParameters.element
		if(sourceElement !is ParadoxScriptScriptedVariable) return true
		val name = sourceElement.name
		if(name.isEmpty()) return true
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			//使用全部作用域
			val scope = GlobalSearchScope.allScope(project)
			//val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
			//这里不进行排序
			val selector = scriptedVariableSelector().gameTypeFrom(sourceElement)
			ParadoxLocalScriptedVariableSearch.search(name, sourceElement, selector = selector).forEach(consumer)
			ParadoxGlobalScriptedVariableSearch.search(name, project, scope, selector = selector).forEach(consumer)
		}
		return true
	}
}

