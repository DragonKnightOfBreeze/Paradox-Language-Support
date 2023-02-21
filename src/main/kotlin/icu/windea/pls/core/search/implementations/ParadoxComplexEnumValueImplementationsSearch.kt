package icu.windea.pls.core.search.implementations

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举值的查询。加入所有作用域内的同名的复杂枚举值。
 *
 *  * 注意：无法通过直接在声明处使用`Ctrl+Click`来查找使用，需要借助于相关的意向。
 *
 *  @see icu.windea.pls.script.intentions.ComplexEnumValueNameGotoImplementationsIntention
 */
class ParadoxComplexEnumValueImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		//得到解析后的PSI元素
		val sourceElement = queryParameters.element
		if(sourceElement !is ParadoxScriptStringExpressionElement) return true
		val complexEnumValueInfo = runReadAction { sourceElement.complexEnumValueInfo }
		if(complexEnumValueInfo == null) return true
		val project = queryParameters.project
		DumbService.getInstance(project).runReadActionInSmartMode {
			val selector = complexEnumValueSelector(project, sourceElement)
				.withSearchScope(GlobalSearchScope.allScope(project)) //使用全部作用域
			ParadoxComplexEnumValueSearch.search(complexEnumValueInfo.name, complexEnumValueInfo.enumName, selector).forEach(consumer)
		}
		return true
	}
}