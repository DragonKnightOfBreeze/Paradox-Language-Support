package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
	private val insertHandler = InsertHandler<LookupElement> { context, _ ->
		//因为只能在$...$引用中出现，如果后面没有"$"，需要自动补充，并将光标移到补充"$"之前
		val editor = context.editor
		val caretModel = editor.caretModel
		val suffixChar = editor.document.charsSequence.getOrNull(caretModel.offset)
		if(suffixChar != '$') {
			EditorModificationUtil.insertStringAtCaret(editor, "$")
			caretModel.moveToOffset(caretModel.offset - 1)
		}
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//同时需要同时查找当前文件中的和全局的
		
		val element = parameters.position
		val project = parameters.originalFile.project
		val selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element).distinctByName()
		val localQuery = ParadoxLocalScriptedVariableSearch.search(element, selector = selector)
		localQuery.processResult { processScriptedVariable(it, result) }
		val globalQuery = ParadoxGlobalScriptedVariableSearch.search(project, selector = selector)
		globalQuery.processResult { processScriptedVariable(it, result) }
	}
	
	private fun processScriptedVariable(it: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
		val name = it.name
		val icon = it.icon
		val typeFile = it.containingFile
		val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon)
			.withTypeText(typeFile.name, typeFile.icon, true)
			.withInsertHandler(insertHandler)
		result.addElement(lookupElement)
		return true
	}
}
