package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.actions.*

//org.intellij.plugins.markdown.ui.actions.styling.MarkdownHeaderAction
//org.intellij.plugins.markdown.ui.actions.styling.BaseToggleStateAction
//org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class SetColorAction(
	val colorConfig: ParadoxTextColorConfig
) : ToggleAction(colorConfig.text, null, colorConfig.icon) {
	private val setColorActionBaseName = PlsBundle.message("action.ParadoxLocalisation.Styling.SetColor.text")
	
	override fun getActionUpdateThread(): ActionUpdateThread {
		return ActionUpdateThread.BGT
	}
	
	override fun update(event: AnActionEvent) {
		val editor = event.editor ?: return
		event.presentation.isEnabled = editor.document.isWritable
		super.update(event)
	}
	
	override fun isSelected(event: AnActionEvent): Boolean {
		val editor = event.editor ?: return false
		val allCarets = editor.caretModel.allCarets
		val caret = allCarets.singleOrNull() ?: return false
		val selectionStart = caret.selectionStart
		if(selectionStart <= 2) return false
		val startString = editor.document.getText(TextRange.create(selectionStart - 2, (selectionStart + 2).coerceAtMost(editor.document.textLength)))
		return startString.lastIndexOf('§').let { it != -1 && it != 3 && startString[it + 1] == colorConfig.name.singleOrNull() }
	}
	
	override fun setSelected(event: AnActionEvent, state: Boolean) {
		val editor = event.editor ?: return
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
			val replaced = "§${colorConfig.name}$toReplace§!"
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

