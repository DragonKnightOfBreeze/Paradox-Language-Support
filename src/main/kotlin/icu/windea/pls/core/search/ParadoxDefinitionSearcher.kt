package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
		val name = queryParameters.name
		val typeExpression = queryParameters.typeExpression
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		if(typeExpression == null) {
			if(name == null) {
				//查找所有定义
				ParadoxDefinitionNameIndex.processAllElementsByKeys(project, scope) { _, it ->
					consumer.process(it)
				}
			} else {
				//按照名字查找定义
				ParadoxDefinitionNameIndex.processAllElements(name, project, scope) {
					consumer.process(it)
				}
			}
			return
		}
		
		//按照类型表达式查找定义
		doProcessQueryByTypeExpression(typeExpression, project, scope, name, consumer)
		
		//如果是切换类型，也要按照基础类型的类型表达式查找定义
		val gameType = queryParameters.selector.gameType
		val configGroup = getCwtConfig(project).get(gameType.id)
		val baseTypeExpression = configGroup?.typeToSwapTypeMap?.get(typeExpression)
		if(baseTypeExpression != null) {
			doProcessQueryByTypeExpression(baseTypeExpression, project, scope, name, consumer)
		}
	}
	
	private fun doProcessQueryByTypeExpression(typeExpression: String, project: Project, scope: GlobalSearchScope, name: String?, consumer: Processor<in ParadoxScriptDefinitionElement>) {
		for(expression in typeExpression.split('|')) {
			val (type, subtype) = ParadoxDefinitionTypeExpression.resolve(expression)
			ParadoxDefinitionTypeIndex.processAllElements(type, project, scope) {
				if(name != null && !matchesName(it, name)) return@processAllElements true
				if(subtype != null && !matchesSubtype(it, subtype)) return@processAllElements true
				consumer.process(it)
			}
		}
	}
	
	private fun matchesName(element: ParadoxScriptDefinitionElement, name: String): Boolean {
		return ParadoxDefinitionHandler.getName(element) == name
	}
	
	private fun matchesSubtype(element: ParadoxScriptDefinitionElement, subtype: String): Boolean {
		return ParadoxDefinitionHandler.getSubtypes(element)?.contains(subtype) == true
	}
}
