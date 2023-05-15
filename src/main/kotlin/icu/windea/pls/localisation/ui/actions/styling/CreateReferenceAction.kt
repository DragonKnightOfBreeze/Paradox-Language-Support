package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

//org.intellij.plugins.markdown.ui.actions.styling.MarkdownCreateLinkAction

class CreateReferenceAction : ToggleAction(), DumbAware {
    private val wrapActionName: String
        get() = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.text")
    
    private val unwrapActionName: String
        get() = PlsBundle.message("action.Pls.ParadoxLocalisation.Styling.CreateReference.unwrap.text")
    
    override fun isSelected(event: AnActionEvent): Boolean {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        if(file !is ParadoxLocalisationFile || editor == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        event.presentation.isEnabledAndVisible = true
        val isSelectedElement = isSelectedElement(file, editor)
        if(isSelectedElement == null) {
            event.presentation.isEnabledAndVisible = false
            return false
        }
        return if(!isSelectedElement) {
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
        if(file !is ParadoxLocalisationFile || editor == null) {
            return
        }
        val project = file.project
        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd
        val isSelectedElement = isSelectedElement(file, editor)
        if(isSelectedElement == null) {
            return
        }
        if(state) {
            //wrap
            val command = Runnable {
                editor.document.insertString(end, "$")
                editor.document.insertString(start, "$")
                editor.selectionModel.setSelection(start + 1, end + 1)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
            WriteCommandAction.runWriteCommandAction(project, wrapActionName, null, command, file)
        } else {
            //unwrap
            val command = Runnable {
                editor.document.deleteString(end, end + 1)
                editor.document.deleteString(start - 1, start)
                editor.selectionModel.setSelection(start - 1, end - 1)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
            WriteCommandAction.runWriteCommandAction(project, unwrapActionName, null, command, file)
        }
    }
    
    override fun update(event: AnActionEvent) {
        val originalIcon = event.presentation.icon
        super.update(event)
        if(ActionPlaces.isPopupPlace(event.place)) {
            // Restore original icon, as it will be disabled in popups, and we still want to show in GeneratePopup
            event.presentation.icon = originalIcon
        }
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    companion object {
        private fun isSelectedElement(file: PsiFile, editor: Editor): Boolean? {
            //返回null表示此操作不可用 
            //仅判断选中文本之外是否都是正确的首尾PSI ELEMENT，不判断执行操作后语法是否仍然合法
            val start = editor.selectionModel.selectionStart
            val end = editor.selectionModel.selectionEnd
            if(start == 0) return null
            val startElement = file.findElementAt(start - 1) ?: return null
            val endElement = file.findElementAt(end) ?: return null
            val startParent = startElement.parentOfType<ParadoxLocalisationPropertyReference>() ?: return null
            val endParent = endElement.parentOfType<ParadoxLocalisationPropertyReference>() ?: return null
            if(startParent !== endParent) return null
            if(start == end) return false
            return startElement.elementType == PROPERTY_REFERENCE_START && endElement.elementType == PROPERTY_REFERENCE_END
        }
    }
}