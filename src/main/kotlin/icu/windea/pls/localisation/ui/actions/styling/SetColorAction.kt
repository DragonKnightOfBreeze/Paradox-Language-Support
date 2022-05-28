package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

//org.intellij.plugins.markdown.ui.actions.styling.MarkdownHeaderAction
//org.intellij.plugins.markdown.ui.actions.styling.BaseToggleStateAction
//org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class SetColorAction(
	private val colorConfig: ParadoxColorConfig
): AnAction(PlsBundle.message("action.ParadoxScript.SetColorAction.text", colorConfig.description), null, colorConfig.icon), CustomComponentAction, DumbAware{
	private val setColorActionBaseName = PlsBundle.message("action.ParadoxScript.SetColorAction.text", colorConfig.description)  
	
	override fun update(event: AnActionEvent) {
		val editor = event.dataContext.getData(CommonDataKeys.EDITOR) ?:return
		event.presentation.isEnabled = editor.document.isWritable
	}
	
	override fun actionPerformed(event: AnActionEvent) {
		val editor = event.dataContext.getData(CommonDataKeys.EDITOR) ?:return
		val project = event.project ?: return
		for(caret in editor.caretModel.allCarets) {
			setColor(caret, editor, project)
		}
	}
	
	private fun setColor(caret: Caret, editor: Editor, project: Project) {
		val selected = caret.selectedText ?: ""
		val selectionStart = caret.selectionStart
		val selectionEnd = caret.selectionEnd
		val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
		WriteCommandAction.writeCommandAction(project, file)
			.withName(setColorActionBaseName)
			.run<Nothing> { 
				caret.removeSelection()
				val replaced = "§${colorConfig.id}$selected§!"
				editor.document.replaceString(selectionStart, selectionEnd, replaced)
				caret.moveToOffset(selectionEnd + 2)
			}
	}
}

