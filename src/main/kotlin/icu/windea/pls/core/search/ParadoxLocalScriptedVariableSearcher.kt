package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询器。（本地：同一脚本文件）
 */
class ParadoxLocalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
		//查找在使用处之前声明的封装本地变量
		val selector = queryParameters.selector
		val file = selector.file ?: return
		val fileInfo = selector.fileInfo ?: return
		if("common/scripted_variables".matchesPath(fileInfo.path.path)) return
		val psiFile = file.toPsiFile<ParadoxScriptFile>(selector.project) ?: return
		psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				val result = when(element) {
					is ParadoxScriptScriptedVariable -> visitScriptedVariable(element)
					else -> true
				}
				if(!result) return
				if(element == selector.context) return //到使用处为止
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable): Boolean {
				if(queryParameters.name == null || queryParameters.name == element.name) {
					if(!consumer.process(element)) return false
				}
				return true
			}
		})
	}
}
