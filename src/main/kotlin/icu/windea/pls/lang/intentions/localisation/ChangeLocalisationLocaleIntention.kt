package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*

/**
 * 更改本地化语言区域。
 */
class ChangeLocalisationLocaleIntention : IntentionAction, PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationLocale")

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val localeConfigs = PlsFacade.getConfigGroup(project, null).localisationLocalesById.values
        val popup = Popup(project, element, localeConfigs.toTypedArray())
        JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(editor)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationLocale? {
        return ParadoxPsiManager.findLocalisationLocale(file, offset, true)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    private class Popup(
        private val project: Project,
        private val value: ParadoxLocalisationLocale,
        values: Array<CwtLocaleConfig>
    ) : BaseListPopupStep<CwtLocaleConfig>(PlsBundle.message("intention.changeLocalisationLocale.title"), *values) {
        override fun getIconFor(value: CwtLocaleConfig) = PlsIcons.Nodes.LocalisationLocale

        override fun getTextFor(value: CwtLocaleConfig) = value.text

        override fun getDefaultOptionIndex() = 0

        override fun isSpeedSearchEnabled(): Boolean = true

        @Suppress("UnstableApiUsage")
        override fun onChosen(selectedValue: CwtLocaleConfig, finalChoice: Boolean) = doFinalStep {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                writeCommandAction(project, PlsBundle.message("intention.changeLocalisationLocale.command")) {
                    value.setName(selectedValue.id)
                }
            }
        }
    }
}

