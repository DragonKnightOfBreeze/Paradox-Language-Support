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
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 更改语言区域。
 */
class ChangeLocalisationLocaleIntention : IntentionAction, PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationLocale")

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
        val locales = PlsFacade.getConfigGroup(project, null).localisationLocalesById.values.toTypedArray()
        JBPopupFactory.getInstance().createListPopup(Popup(element, locales)).showInBestPositionFor(editor)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationLocale? {
        return ParadoxPsiManager.findLocalisationLocale(file, offset, true)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    private class Popup(
        private val value: ParadoxLocalisationLocale,
        values: Array<CwtLocalisationLocaleConfig>
    ) : BaseListPopupStep<CwtLocalisationLocaleConfig>(PlsBundle.message("intention.changeLocalisationLocale.title"), *values) {
        override fun getIconFor(value: CwtLocalisationLocaleConfig) = PlsIcons.Nodes.LocalisationLocale

        override fun getTextFor(value: CwtLocalisationLocaleConfig) = value.text

        override fun getDefaultOptionIndex() = 0

        override fun isSpeedSearchEnabled(): Boolean = true

        override fun onChosen(selectedValue: CwtLocalisationLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
            runUndoTransparentWriteAction { value.setName(selectedValue.id) }
            return FINAL_CHOICE
        }
    }
}

