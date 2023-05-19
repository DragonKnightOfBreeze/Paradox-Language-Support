package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询器。（本地：同一脚本文件）
 */
class ParadoxLocalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
		ProgressManager.checkCanceled()
		val scope = queryParameters.selector.scope
		if(SearchScope.isEmptyScope(scope)) return
		
		val name = queryParameters.name
		val selector = queryParameters.selector
		val file = selector.file ?: return
		val fileInfo = selector.fileInfo ?: return
		if("common/scripted_variables".matchesPath(fileInfo.pathToEntry.path)) return
		val psiFile = file.toPsiFile<ParadoxScriptFile>(selector.project) ?: return
		val startOffset = selector.context?.castOrNull<PsiElement>()?.startOffset
		ParadoxScriptedVariableHandler.getLocalScriptedVariables(psiFile).process p@{ 
			ProgressManager.checkCanceled()
			//仅查找在上下文位置之前声明的封装本地变量
			val element = it.element ?: return@p true
			if(startOffset != null && startOffset <= element.startOffset) return@p true
			if(name != null && name != element.name) return@p true
			consumer.process(element)
		}
	}
}
