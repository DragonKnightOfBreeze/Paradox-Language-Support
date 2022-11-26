package icu.windea.pls.core.search.implementations

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch: QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		//得到解析后的PSI元素
		val sourceElement = queryParameters.element
		if(sourceElement is ParadoxScriptScriptedVariable) {
			val name = sourceElement.name
			val project = queryParameters.project
			//使用全部作用域
			val scope = GlobalSearchScope.allScope(project)
			//val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
			runReadAction {
				//这里不进行排序
				val selector = scriptedVariableSelector().gameTypeFrom(sourceElement)
				ParadoxLocalScriptedVariableSearch.search(name, sourceElement, selector = selector).forEach(consumer)
				ParadoxGlobalScriptedVariableSearch.search(name, project, selector = selector).forEach(consumer)
			}
		}
		return true
	}
}

