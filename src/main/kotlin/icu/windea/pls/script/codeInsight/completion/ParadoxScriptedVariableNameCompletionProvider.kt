package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量的名字的代码补全。
 */
class ParadoxScriptedVariableNameCompletionProvider: CompletionProvider<CompletionParameters>(){
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		if(!getSettings().completion.completeScriptedVariableNames) return
		
		//这里不需要查找本地的封装变量（即当前文件中声明的封装变量）
		
		//查找全局的
		val element = parameters.position.parent
		val file = parameters.originalFile
		val project = file.project
		val selector = scriptedVariableSelector(project, element).contextSensitive().notSamePosition(element).distinctByName()
		ParadoxGlobalScriptedVariableSearch.search(selector).processQuery(limited = true) { processScriptedVariable(it, result) }
	}
	
	@Suppress("SameReturnValue")
	private fun processScriptedVariable(scriptedVariable: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
		//不自动插入后面的等号
		ProgressManager.checkCanceled()
		val name = scriptedVariable.name ?: return true
		val icon = scriptedVariable.icon
		val tailText = scriptedVariable.value?.let { " = $it" }
		val typeFile = scriptedVariable.containingFile
		val lookupElement = LookupElementBuilder.create(scriptedVariable, name).withIcon(icon)
			.withTailText(tailText)
			.withTypeText(typeFile.name, typeFile.icon, true)
		result.addElement(lookupElement)
		return true
	}
}