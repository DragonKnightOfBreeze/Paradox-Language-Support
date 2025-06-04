package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.collections.toSingletonListOrEmpty
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.findLocalisationPropertiesBetween
import icu.windea.pls.localisation.psi.hasLocalisationPropertiesBetween

abstract class ManipulateLocalisationIntentionBase : IntentionAction, DumbAware {
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

    protected abstract fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>)

    //默认不显示预览，因为可能涉及异步调用
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}
