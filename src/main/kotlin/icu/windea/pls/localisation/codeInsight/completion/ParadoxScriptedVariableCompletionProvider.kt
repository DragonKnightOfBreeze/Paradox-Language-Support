package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
	//因为只能在$...$引用中出现，如果后面没有"$"，需要自动补充，并将光标移到补充"$"之前
	private val insertHandler = InsertHandler<LookupElement> { context, _ ->
		val editor = context.editor
		val caretModel = editor.caretModel
		val suffixChar = editor.document.charsSequence.getOrNull(caretModel.offset)
		if(suffixChar != '$') {
			EditorModificationUtil.insertStringAtCaret(editor, "$")
			caretModel.moveToOffset(caretModel.offset - 1)
		}
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val element = parameters.position
		val project = parameters.originalFile.project
		val selector = scriptedVariableSelector(project, element).contextSensitive().distinctByName()
		ParadoxGlobalScriptedVariableSearch.search(selector = selector).processQueryAsync { processScriptedVariable(it, result) }
	}
	
	@Suppress("SameReturnValue")
	private fun processScriptedVariable(it: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
		ProgressManager.checkCanceled()
		val name = it.name ?: return true
		val icon = it.icon
		val typeFile = it.containingFile
		val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon)
			.withTypeText(typeFile.name, typeFile.icon, true)
			.withInsertHandler(insertHandler)
		result.addElement(lookupElement)
		return true
	}
}

