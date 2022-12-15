package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//同时需要同时查找当前文件中的和全局的
		val element = parameters.position
		val project = parameters.originalFile.project
		val selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element).distinctByName()
		val localQuery = ParadoxLocalScriptedVariableSearch.search(element, selector = selector)
		localQuery.processQuery { processScriptedVariable(it, result) }
		val globalQuery = ParadoxGlobalScriptedVariableSearch.search(project, selector = selector)
		globalQuery.processQuery { processScriptedVariable(it, result) }
	}
	
	private fun processScriptedVariable(it: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
		val name = it.name
		val icon = it.icon
		val typeFile = it.containingFile
		val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon)
			.withTypeText(typeFile.name, typeFile.icon, true)
		result.addElement(lookupElement)
		return true
	}
}

/**
 * 提供封装变量的名字的代码补全。
 */
class ParadoxScriptedVariableNameCompletionProvider: CompletionProvider<CompletionParameters>(){
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val ele
		//查找全局的，排除正在输入的
		TODO("Not yet implemented")
	}
	
}