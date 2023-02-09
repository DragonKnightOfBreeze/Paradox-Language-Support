package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询器。（本地：同一脚本文件）
 */
class ParadoxLocalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
		//在当前脚本文件中递归向上向前查找（包括上下文元素自身）
		val context = queryParameters.context
		var current: PsiElement = context
		while(current !is PsiFile) {
			var prevSibling : PsiElement? = current
			while(prevSibling != null) {
				if(prevSibling is ParadoxScriptScriptedVariable) {
					if(queryParameters.name == null || queryParameters.name == prevSibling.name) {
						if(!consumer.process(prevSibling)) return
					}
				}
				prevSibling = prevSibling.prevSibling
			}
			current = current.parent ?: break
		}
	}
}
