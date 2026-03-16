@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.parentProperty

/**
 * 更改定义注入模式。
 */
class ChangeDefinitionInjectionModeIntention : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.changeDefinitionInjectionMode")

    override fun getPresentation(context: ActionContext): Presentation? {
        findElement(context) ?: return null
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext): ModCommand {
        val element = findElement(context) ?: return ModCommand.nop()
        val gameType = selectGameType(context.file) ?: return ModCommand.nop()
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val modes = configGroup.directivesModel.definitionInjection?.modeConfigs?.keys?.orNull() ?: return ModCommand.nop()
        val items = modes.map { ItemIntention(element, it) }
        return ModCommand.chooseAction(PlsBundle.message("intention.changeDefinitionInjectionMode.title"), items)
    }

    private fun findElement(context: ActionContext): ParadoxScriptProperty? {
        val gameType = selectGameType(context.file) ?: return null
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return null
        val expression = ParadoxPsiFileManager.findScriptExpression(context.file, context.offset)
        if (expression !is ParadoxScriptPropertyKey) return null
        val property = expression.parentProperty ?: return null
        if (!ParadoxPsiMatcher.isDefinitionInjection(property)) return null
        return property
    }

    private class ItemIntention(
        element: ParadoxScriptProperty,
        private val mode: String,
    ) : PsiUpdateModCommandAction<ParadoxScriptProperty>(element) {
        override fun getFamilyName() = PlsBundle.message("intention.changeDefinitionInjectionMode.item", mode)

        override fun getPresentation(context: ActionContext, element: ParadoxScriptProperty): Presentation {
            return Presentation.of(mode).withIcon(PlsIcons.Nodes.Directive)
        }

        override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
            val expressionElement = element.propertyKey
            val oldMode = ParadoxDefinitionInjectionManager.getModeFromExpression(expressionElement.name) ?: return
            val range = TextRange.from(0, oldMode.length)
            ElementManipulators.handleContentChange(expressionElement, range, mode)
        }
    }
}
