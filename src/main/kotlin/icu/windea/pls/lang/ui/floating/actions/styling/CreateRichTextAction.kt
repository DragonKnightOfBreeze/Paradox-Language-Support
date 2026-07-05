package icu.windea.pls.lang.ui.floating.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue

// org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

sealed class CreateRichTextAction : ToggleAction(), DumbAware {
    protected abstract val startMarker: String
    protected abstract val endMarker: String

    protected abstract val wrapActionName: String
    protected abstract val wrapActionDescription: String
    protected abstract val unwrapActionName: String
    protected abstract val unwrapActionDescription: String

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val originalIcon = e.presentation.icon
        super.update(e)
        if (e.isFromContextMenu) {
            // Restore original icon, as it will be disabled in popups, and we still want to show in GeneratePopup
            e.presentation.icon = originalIcon
        }
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (file !is ParadoxLocalisationFile || editor == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        val isAvailable = isAvailable(file)
        if (!isAvailable) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        val isSelectedElement = isSelectedElement(file, editor)
        if (isSelectedElement == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        event.presentation.isEnabledAndVisible = true
        return if (!isSelectedElement) {
            // wrap
            event.presentation.isEnabled = !editor.isViewer
            event.presentation.text = wrapActionName
            event.presentation.description = wrapActionDescription
            false
        } else {
            // unwrap
            event.presentation.isEnabled = !editor.isViewer
            event.presentation.text = unwrapActionName
            event.presentation.description = unwrapActionDescription
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
            // wrap
            executeWriteCommand(project, wrapActionName, makeWritable = file) {
                editor.document.insertString(end, endMarker)
                editor.document.insertString(start, startMarker)
                editor.caretModel.moveToOffset(start + startMarker.length)
                editor.selectionModel.setSelection(start + startMarker.length, end + endMarker.length)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
        } else {
            // unwrap
            executeWriteCommand(project, unwrapActionName, makeWritable = file) {
                editor.document.deleteString(end, end + endMarker.length)
                editor.document.deleteString(start - startMarker.length, start)
                editor.caretModel.moveToOffset(start - startMarker.length)
                editor.selectionModel.setSelection(start - startMarker.length, end - endMarker.length)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
        }
    }

    private fun isSelectedElement(file: PsiFile, editor: Editor): Boolean? {
        // 返回null表示此操作不可用
        // 仅判断选中文本之外是否都是正确的首尾PSI ELEMENT，不判断执行操作后语法是否仍然合法
        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd
        if (start == 0) return null
        val startElement = file.findElementAt(start - 1) ?: return null
        val endElement = file.findElementAt(end) ?: return null
        val startParent = startElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
        val endParent = endElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
        if (startParent !== endParent) return null
        if (start == end) return false
        return isSelectedElement(startElement, endElement)
    }

    protected abstract fun isAvailable(file: PsiFile): Boolean

    protected abstract fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean
}
