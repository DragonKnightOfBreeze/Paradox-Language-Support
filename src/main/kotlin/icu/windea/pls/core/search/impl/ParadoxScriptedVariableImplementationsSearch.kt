package icu.windea.pls.core.search.impl

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch: QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		//得到解析后的PSI元素
		val sourceElement = queryParameters.element
		if(sourceElement is ParadoxScriptVariable){
			val name = sourceElement.name
			val project = queryParameters.project
			//使用全部作用域
			val scope = GlobalSearchScope.allScope(project)
			//val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
			//这里不需要也无法进行排序
			val selector = scriptedVariableSelector().gameTypeFrom(sourceElement).preferRootFrom(sourceElement)
			val scriptedVariables = runReadAction { 
				findScriptedVariables(name, project, scope, selector = selector)
			}
			scriptedVariables.forEach { 
				consumer.process(it)
			}
		}
		return true
	}
}

