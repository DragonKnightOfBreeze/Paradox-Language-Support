package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.internal.config.*

//org.intellij.plugins.markdown.ui.actions.styling.MarkdownHeaderAction
//org.intellij.plugins.markdown.ui.actions.styling.BaseToggleStateAction
//org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class SetColorAction(
	val colorConfig: ParadoxColorConfig
) : ToggleAction(colorConfig.text, null, colorConfig.icon), DumbAware {
	private val setColorActionBaseName = colorConfig.text
	
	override fun update(event: AnActionEvent) {
		val editor = event.dataContext.getData(CommonDataKeys.EDITOR) ?: return
		event.presentation.isEnabled = editor.document.isWritable
		super.update(event)
	}
	
	override fun isSelected(event: AnActionEvent): Boolean {
		val editor = event.dataContext.getData(CommonDataKeys.EDITOR) ?: return false
		val allCarets = editor.caretModel.allCarets
		val caret = allCarets.singleOrNull() ?: return false
		val selectionStart = caret.selectionStart
		if(selectionStart <= 2) return false
		val startString = editor.document.getText(TextRange.create(selectionStart - 2, (selectionStart + 2).coerceAtMost(editor.document.textLength)))
		return startString.lastIndexOf('§').let { it != -1 && it != 3 && startString[it + 1] == colorConfig.id.singleOrNull() }
	}
	
	override fun setSelected(event: AnActionEvent, state: Boolean) {
		val editor = event.dataContext.getData(CommonDataKeys.EDITOR) ?: return
		val project = event.project ?: return
		val allCarets = editor.caretModel.allCarets
		val caret = allCarets.singleOrNull() ?: return
		val selectionStart = caret.selectionStart
		if(selectionStart <= 2) return
		val selectionEnd = caret.selectionEnd
		val startString = editor.document.getText(TextRange.create(selectionStart - 2, (selectionStart + 2).coerceAtMost(editor.document.textLength)))
		val startIndex = startString.lastIndexOf('§')
		val start = if(startIndex != -1) selectionStart + startIndex - 2 else selectionStart
		val toReplaceStart = if(startIndex != -1) selectionStart + startIndex else selectionStart
		val endString = editor.document.getText(TextRange.create(selectionEnd - 2, (selectionEnd + 2).coerceAtMost(editor.document.textLength)))
		val endIndex = endString.indexOf("§!")
		val end = if(endIndex != -1) selectionEnd + endIndex else selectionEnd
		val toReplaceEnd = if(endIndex != -1) selectionEnd + endIndex - 2 else selectionEnd
		val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
		val command = Runnable {
			val toReplace = editor.document.getText(TextRange.create(toReplaceStart, toReplaceEnd))
			val replaced = "§${colorConfig.id}$toReplace§!"
			editor.document.replaceString(start, end, replaced)
			val caretStart = start + 2
			val caretEnd = start + 2 + toReplace.length
			editor.selectionModel.setSelection(caretStart, caretEnd)
			editor.caretModel.moveToOffset(caretEnd)
			PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
		}
		WriteCommandAction.runWriteCommandAction(project, setColorActionBaseName, null, command, file)
	}
}

