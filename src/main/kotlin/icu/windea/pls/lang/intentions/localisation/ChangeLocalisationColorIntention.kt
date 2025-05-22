package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * 更改颜色。
 */
class ChangeLocalisationColorIntention : IntentionAction, PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationColor")

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val colorConfigs = ParadoxTextColorManager.getInfos(project, file)
        JBPopupFactory.getInstance().createListPopup(Popup(element, colorConfigs.toTypedArray())).showInBestPositionFor(editor)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationColorfulText? {
        return ParadoxPsiManager.findLocalisationColorfulText(file, offset, true)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    private class Popup(
        private val value: ParadoxLocalisationColorfulText,
        values: Array<ParadoxTextColorInfo>
    ) : BaseListPopupStep<ParadoxTextColorInfo>(PlsBundle.message("intention.changeLocalisationColor.title"), *values) {
        override fun getIconFor(value: ParadoxTextColorInfo) = value.icon

        override fun getTextFor(value: ParadoxTextColorInfo) = value.text

        override fun getDefaultOptionIndex() = 0

        override fun isSpeedSearchEnabled(): Boolean = true

        override fun onChosen(selectedValue: ParadoxTextColorInfo, finalChoice: Boolean): PopupStep<*>? {
            runUndoTransparentWriteAction { value.setName(selectedValue.name) }
            return FINAL_CHOICE
        }
    }
}
