@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiUpdateModCommandAction
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale

/**
 * 更改本地化语言环境。
 */
class ChangeLocalisationLocaleIntention : ModCommandAction {
    override fun getFamilyName() = ChronicleBundle.message("intention.changeLocalisationLocale")

    override fun getPresentation(context: ActionContext): Presentation? {
        findElement(context) ?: return null
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext): ModCommand {
        val element = findElement(context) ?: return ModCommand.nop()
        val project = context.project
        val gameType = selectGameType(context.file)
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val localeConfigs = configGroup.supportedLocales
        if (localeConfigs.isEmpty()) return ModCommand.nop()
        val items = localeConfigs.map { ItemIntention(element, it) }
        return ModCommand.chooseAction(ChronicleBundle.message("intention.changeLocalisationLocale.title"), items)
    }

    private fun findElement(context: ActionContext): ParadoxLocalisationLocale? {
        return ParadoxPsiFileManager.findLocalisationLocale(context.file, context.offset, true)
    }

    private class ItemIntention(
        element: ParadoxLocalisationLocale,
        private val localeConfig: CwtLocaleConfig,
    ) : PsiUpdateModCommandAction<ParadoxLocalisationLocale>(element) {
        override fun getFamilyName() = ChronicleBundle.message("intention.changeLocalisationLocale.item", localeConfig.id)

        override fun getPresentation(context: ActionContext, element: ParadoxLocalisationLocale): Presentation {
            return Presentation.of(localeConfig.idWithText).withIcon(PlsIcons.Nodes.LocalisationLocale)
        }

        override fun invoke(context: ActionContext, element: ParadoxLocalisationLocale, updater: ModPsiUpdater) {
            element.setName(localeConfig.id)
        }
    }
}
