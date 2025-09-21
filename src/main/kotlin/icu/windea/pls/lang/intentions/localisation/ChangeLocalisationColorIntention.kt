package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.model.ParadoxTextColorInfo
import kotlinx.coroutines.launch

/**
 * 更改本地化颜色。
 */
class ChangeLocalisationColorIntention : IntentionAction, PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationColor")

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val colorConfigs = ParadoxTextColorManager.getInfos(project, file)
        val popup = Popup(project, element, colorConfigs.toTypedArray())
        JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(editor)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationColorfulText? {
        return ParadoxPsiFinder.findLocalisationColorfulText(file, offset, true)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    private class Popup(
        private val project: Project,
        private val value: ParadoxLocalisationColorfulText,
        values: Array<ParadoxTextColorInfo>
    ) : BaseListPopupStep<ParadoxTextColorInfo>(PlsBundle.message("intention.changeLocalisationColor.title"), *values) {
        override fun getIconFor(value: ParadoxTextColorInfo) = value.icon

        override fun getTextFor(value: ParadoxTextColorInfo) = value.text

        override fun getDefaultOptionIndex() = 0

        override fun isSpeedSearchEnabled(): Boolean = true

        @Suppress("UnstableApiUsage")
        override fun onChosen(selectedValue: ParadoxTextColorInfo, finalChoice: Boolean) = doFinalStep {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                writeCommandAction(project, PlsBundle.message("intention.changeLocalisationColor.command")) {
                    value.setName(selectedValue.name)
                }
            }
        }
    }
}
