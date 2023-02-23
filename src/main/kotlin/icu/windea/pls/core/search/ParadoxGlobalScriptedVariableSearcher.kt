package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.psi.*

/**
 * 全局封装变量的查询器。
 */
class ParadoxGlobalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxGlobalScriptedVariableSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxGlobalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
		val project = queryParameters.project
		val scope = queryParameters.selector.scope
		if(queryParameters.name == null) {
			//查找所有封装变量
			ParadoxScriptedVariableNameIndex.KEY.processAllElementsByKeys(project, scope) { _, it ->
				consumer.process(it)
			}
			return
		}
		//查找指定名字的封装变量
		ParadoxScriptedVariableNameIndex.KEY.processAllElements(queryParameters.name, project, scope) {
			consumer.process(it)
		}
	}
}
