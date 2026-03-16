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
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale

/**
 * 更改本地化语言环境。
 */
class ChangeLocalisationLocaleIntention : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationLocale")

    override fun getPresentation(context: ActionContext): Presentation? {
        findElement(context) ?: return null
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext): ModCommand {
        val element = findElement(context) ?: return ModCommand.nop()
        val project = context.project
        val localeConfigs = PlsFacade.getConfigGroup(project).localisationLocalesById.values
        val items = localeConfigs.map { ItemIntention(element, it) }
        return ModCommand.chooseAction(PlsBundle.message("intention.changeLocalisationLocale.title"), items)
    }

    private fun findElement(context: ActionContext): ParadoxLocalisationLocale? {
        return ParadoxPsiFileManager.findLocalisationLocale(context.file, context.offset, true)
    }

    private class ItemIntention(
        element: ParadoxLocalisationLocale,
        private val localeConfig: CwtLocaleConfig,
    ) : PsiUpdateModCommandAction<ParadoxLocalisationLocale>(element) {
        override fun getFamilyName() = PlsBundle.message("intention.changeLocalisationLocale.item", localeConfig.id)

        override fun getPresentation(context: ActionContext, element: ParadoxLocalisationLocale): Presentation {
            return Presentation.of(localeConfig.idWithText).withIcon(PlsIcons.Nodes.LocalisationLocale)
        }

        override fun invoke(context: ActionContext, element: ParadoxLocalisationLocale, updater: ModPsiUpdater) {
            element.setName(localeConfig.id)
        }
    }
}
