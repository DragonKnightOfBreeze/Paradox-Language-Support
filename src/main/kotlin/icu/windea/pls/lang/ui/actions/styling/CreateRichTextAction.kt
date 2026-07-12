package icu.windea.pls.lang.ui.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.prevLeaf
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets

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

    override fun isSelected(e: AnActionEvent): Boolean {
        e.presentation.isEnabledAndVisible = false
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return false
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return false
        if (file !is ParadoxLocalisationFile) return false
        if (!isAvailable(editor, file)) return false
        val isSelectedElement = isSelectedElement(editor, file)
        if (isSelectedElement == null) return false
        e.presentation.isEnabledAndVisible = true
        return if (!isSelectedElement) {
            // wrap
            e.presentation.isEnabled = !editor.isViewer
            e.presentation.text = wrapActionName
            e.presentation.description = wrapActionDescription
            false
        } else {
            // unwrap
            e.presentation.isEnabled = !editor.isViewer
            e.presentation.text = unwrapActionName
            e.presentation.description = unwrapActionDescription
            true
        }
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxLocalisationFile) return
        val isSelectedElement = isSelectedElement(editor, file)
        if (isSelectedElement == null) return
        val project = file.project
        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        if (state) {
            // wrap
            executeWriteCommand(project, wrapActionName, makeWritable = file) {
                editor.document.insertString(selectionEnd, endMarker)
                editor.document.insertString(selectionStart, startMarker)
                editor.caretModel.moveToOffset(selectionStart)
                editor.selectionModel.setSelection(selectionStart, selectionEnd + startMarker.length + endMarker.length)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
        } else {
            // unwrap
            executeWriteCommand(project, unwrapActionName, makeWritable = file) {
                editor.document.deleteString(selectionEnd - endMarker.length, selectionEnd)
                editor.document.deleteString(selectionStart, selectionStart + startMarker.length)
                editor.caretModel.moveToOffset(selectionStart)
                editor.selectionModel.setSelection(selectionStart, selectionEnd - startMarker.length - endMarker.length)
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            }
        }
    }

    private fun isAvailable(editor: Editor, file: ParadoxLocalisationFile): Boolean {
        if (!isAvailable(file)) return false

        if (!PsiDocumentManager.getInstance(file.project).isCommitted(editor.document)) return false

        val selectionModel = editor.selectionModel
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        // 忽略没有选择文本的情况
        if (selectionStart == selectionEnd) return false

        val startElement = file.findElementAt(selectionStart) ?: return false
        val endElement = file.findElementAt(selectionEnd - 1) ?: return false

        // 要求开始位置和结束位置的左边或右边是 TEXT_TOKEN/LEFT_QUOTE/RIGHT_QUOTE
        val stringOrQuoteTokens = ParadoxLocalisationTokenSets.STRING_OR_QUOTE_TOKENS
        if (startElement.elementType !in stringOrQuoteTokens && startElement.prevLeaf(false).elementType !in stringOrQuoteTokens) return false
        if (endElement.elementType !in stringOrQuoteTokens && endElement.nextLeaf(false).elementType !in stringOrQuoteTokens) return false

        // 要求向上能查找到同一个 ParadoxLocalisationPropertyValue
        val startPropertyValue = startElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        val endPropertyValue = endElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        if (startPropertyValue !== endPropertyValue) return false

        // 要求选择文本的范围在引号之间
        val propertyValue = startPropertyValue
        val textRange = propertyValue.textRange
        val start = if (propertyValue.firstChild.elementType == LEFT_QUOTE) textRange.startOffset + 1 else textRange.startOffset
        val end = if (propertyValue.lastChild.elementType == RIGHT_QUOTE) textRange.endOffset - 1 else textRange.endOffset
        if (selectionStart < start || selectionEnd > end) return false

        return true
    }

    private fun isSelectedElement(editor: Editor, file: ParadoxLocalisationFile): Boolean? {
        val selectionModel = editor.selectionModel
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        // 忽略没有选择文本的情况
        if (selectionStart == selectionEnd) return null

        val startElement = file.findElementAt(selectionStart) ?: return null
        val endElement = file.findElementAt(selectionEnd - 1) ?: return null

        // 要求向上能查找到同一个 ParadoxLocalisationPropertyValue
        val startPropertyValue = startElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
        val endPropertyValue = endElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return null
        if (startPropertyValue !== endPropertyValue) return null

        return isSelectedElement(startElement, endElement)
    }

    protected abstract fun isAvailable(file: ParadoxLocalisationFile): Boolean

    protected abstract fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean
}
