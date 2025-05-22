package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.createNotification
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 复制本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
open class CopyLocalisationIntention : IntentionAction, PriorityAction, DumbAware {
    override fun getPriority() = PriorityAction.Priority.LOW

    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisation")

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        if (file.language !is ParadoxLocalisationLanguage) return false
        val hasElements = if (selectionStart == selectionEnd) {
            val originalElement = file.findElementAt(selectionStart)
            originalElement?.parentOfType<ParadoxLocalisationProperty>() != null
        } else {
            val originalStartElement = file.findElementAt(selectionStart) ?: return false
            val originalEndElement = file.findElementAt(selectionEnd)
            hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
        return hasElements
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val elements = if (selectionStart == selectionEnd) {
            val originalElement = file.findElementAt(selectionStart)
            originalElement?.parentOfType<ParadoxLocalisationProperty>().toSingletonListOrEmpty()
        } else {
            val originalStartElement = file.findElementAt(selectionStart) ?: return
            val originalEndElement = file.findElementAt(selectionEnd)
            findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
        if (elements.isEmpty()) return

        doInvoke(project, editor, file, elements)
    }

    protected open fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        val textToCopy = elements.joinToString("\n") { it.text }
        createNotification(PlsBundle.message("intention.copyLocalisation.notification.success"), NotificationType.INFORMATION).notify(project)
        CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}

