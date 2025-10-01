package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.model.ParadoxTextColorInfo

// org.intellij.plugins.markdown.ui.actions.styling.MarkdownHeaderAction
// org.intellij.plugins.markdown.ui.actions.styling.BaseToggleStateAction
// org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class SetColorAction(
    val colorConfig: ParadoxTextColorInfo
) : ToggleAction(colorConfig.text, null, colorConfig.icon) {
    private val setColorActionBaseName = PlsBundle.message("action.Pls.Localisation.Styling.SetColor.text")

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

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
        if (selectionStart <= 2) return false
        val startString = editor.document.getText(TextRange.create(selectionStart - 2, (selectionStart + 2).coerceAtMost(editor.document.textLength)))
        return startString.lastIndexOf('§').let { it != -1 && it != 3 && startString[it + 1] == colorConfig.name.singleOrNull() }
    }

    override fun setSelected(event: AnActionEvent, state: Boolean) {
        val editor = event.editor ?: return
        val project = event.project ?: return
        val allCarets = editor.caretModel.allCarets
        val caret = allCarets.singleOrNull() ?: return
        val selectionStart = caret.selectionStart
        if (selectionStart <= 2) return
        val selectionEnd = caret.selectionEnd
        val startString = editor.document.getText(TextRange.create(selectionStart - 2, (selectionStart + 2).coerceAtMost(editor.document.textLength)))
        val startIndex = startString.lastIndexOf('§')
        val start = if (startIndex != -1) selectionStart + startIndex - 2 else selectionStart
        val toReplaceStart = if (startIndex != -1) selectionStart + startIndex else selectionStart
        val endString = editor.document.getText(TextRange.create(selectionEnd - 2, (selectionEnd + 2).coerceAtMost(editor.document.textLength)))
        val endIndex = endString.indexOf("§!")
        val end = if (endIndex != -1) selectionEnd + endIndex else selectionEnd
        val toReplaceEnd = if (endIndex != -1) selectionEnd + endIndex - 2 else selectionEnd
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

