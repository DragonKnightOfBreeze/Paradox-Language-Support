package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue

//org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class CreateReferenceAction : ToggleAction(), DumbAware {
    private val wrapActionName: String
        get() = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.text")

    private val unwrapActionName: String
        get() = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.unwrap.text")

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(event: AnActionEvent): Boolean {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (file !is ParadoxLocalisationFile || editor == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        event.presentation.isEnabledAndVisible = true
        val isSelectedElement = isSelectedElement(file, editor)
        if (isSelectedElement == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        return if (!isSelectedElement) {
            //wrap
            event.presentation.isEnabled = !editor.isViewer
            event.presentation.text = wrapActionName
            event.presentation.description = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.description")
            false
        } else {
            //unwrap
            event.presentation.isEnabled = !editor.isViewer
            event.presentation.text = unwrapActionName
            event.presentation.description = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.unwrap.description")
            true
        }
    }

    override fun setSelected(event: AnActionEvent, state: Boolean) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (file !is ParadoxLocalisationFile || editor == null) {
            return
        }
        val project = file.project
        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd
        val isSelectedElement = isSelectedElement(file, editor)
        if (isSelectedElement == null) {
            return
        }
        if (state) {
            //wrap
            val command = Runnable {
                editor.document.insertString(end, "$")
                editor.document.insertString(start, "$")
                editor.caretModel.moveToOffset(start + 1)
                editor.selectionModel.setSelection(start + 1, end + 1)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
            WriteCommandAction.runWriteCommandAction(project, wrapActionName, null, command, file)
        } else {
            //unwrap
            val command = Runnable {
                editor.document.deleteString(end, end + 1)
                editor.document.deleteString(start - 1, start)
                editor.caretModel.moveToOffset(start - 1)
                editor.selectionModel.setSelection(start - 1, end - 1)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
            WriteCommandAction.runWriteCommandAction(project, unwrapActionName, null, command, file)
        }
    }

    override fun update(event: AnActionEvent) {
        val originalIcon = event.presentation.icon
        super.update(event)
        if (event.isFromContextMenu) {
            // Restore original icon, as it will be disabled in popups, and we still want to show in GeneratePopup
            event.presentation.icon = originalIcon
        }
    }

    companion object {
        private fun isSelectedElement(file: PsiFile, editor: Editor): Boolean? {
            //返回null表示此操作不可用
            //仅判断选中文本之外是否都是正确的首尾PSI ELEMENT，不判断执行操作后语法是否仍然合法
            val start = editor.selectionModel.selectionStart
            val end = editor.selectionModel.selectionEnd
            if (start == 0) return null
            val startElement = file.findElementAt(start - 1) ?: return null
            val endElement = file.findElementAt(end) ?: return null
            val startParent = startElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
            val endParent = endElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
            if (startParent !== endParent) return null
            if (start == end) return false
            return startElement.elementType == PARAMETER_START && endElement.elementType == PARAMETER_END
        }
    }
}
