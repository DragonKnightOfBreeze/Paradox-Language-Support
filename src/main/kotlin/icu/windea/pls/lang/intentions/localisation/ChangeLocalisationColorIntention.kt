@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiUpdateModCommandAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.DelegatedIcon
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.model.ParadoxTextColorInfo

/**
 * 更改本地化颜色。
 */
class ChangeLocalisationColorIntention : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationColor")

    override fun getPresentation(context: ActionContext): Presentation? {
        findElement(context) ?: return null
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext): ModCommand {
        val element = findElement(context) ?: return ModCommand.nop()
        val colorInfos = ParadoxTextColorManager.getInfos(context.project, context.file).orNull() ?: return ModCommand.nop()
        val items = colorInfos.map { ItemIntention(element, it) }
        return ModCommand.chooseAction(PlsBundle.message("intention.changeLocalisationColor.title"), items)
    }

    private fun findElement(context: ActionContext): ParadoxLocalisationColorfulText? {
        return ParadoxPsiFileManager.findLocalisationColorfulText(context.file, context.offset, true)
    }

    private class ItemIntention(
        element: ParadoxLocalisationColorfulText,
        private val colorConfig: ParadoxTextColorInfo,
    ) : PsiUpdateModCommandAction<ParadoxLocalisationColorfulText>(element) {
        override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationColor.item", colorConfig.name)

        override fun getPresentation(context: ActionContext, element: ParadoxLocalisationColorfulText): Presentation {
            // NOTE 2.1.6 Cannot use `colorConfig.icon` directly here, or will not be rendered
            return Presentation.of(colorConfig.text).withIcon(DelegatedIcon(colorConfig.icon))
        }

        override fun invoke(context: ActionContext, element: ParadoxLocalisationColorfulText, updater: ModPsiUpdater) {
            element.setName(colorConfig.name)
        }
    }
}
