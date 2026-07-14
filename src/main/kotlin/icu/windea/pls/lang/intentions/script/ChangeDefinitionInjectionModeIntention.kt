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
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.parentProperty

/**
 * 更改定义注入模式。
 */
class ChangeDefinitionInjectionModeIntention : ModCommandAction {
    override fun getFamilyName() = ChronicleBundle.message("intention.changeDefinitionInjectionMode")

    override fun getPresentation(context: ActionContext): Presentation? {
        findElement(context) ?: return null
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext): ModCommand {
        val element = findElement(context) ?: return ModCommand.nop()
        val gameType = selectGameType(context.file) ?: return ModCommand.nop()
        val configGroup = ChronicleFacade.getConfigGroup(gameType)
        val modes = configGroup.macrosModel.forDefinitionInjections?.modeConfigs?.keys?.orNull() ?: return ModCommand.nop()
        val items = modes.map { ItemIntention(element, it) }
        return ModCommand.chooseAction(ChronicleBundle.message("intention.changeDefinitionInjectionMode.title"), items)
    }

    private fun findElement(context: ActionContext): ParadoxScriptProperty? {
        val gameType = selectGameType(context.file) ?: return null
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return null
        val expression = ParadoxPsiFileService.findScriptExpression(context.file, context.offset)
        if (expression !is ParadoxScriptPropertyKey) return null
        val property = expression.parentProperty ?: return null
        if (!ParadoxPsiMatchService.isDefinitionInjection(property)) return null
        return property
    }

    private class ItemIntention(
        element: ParadoxScriptProperty,
        private val mode: String,
    ) : PsiUpdateModCommandAction<ParadoxScriptProperty>(element) {
        override fun getFamilyName() = ChronicleBundle.message("intention.changeDefinitionInjectionMode.item", mode)

        override fun getPresentation(context: ActionContext, element: ParadoxScriptProperty): Presentation {
            return Presentation.of(mode).withIcon(ChronicleIcons.Nodes.Macro)
        }

        override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
            val expressionElement = element.propertyKey
            val expressionText = ParadoxExpressionManager.getExpressionText(expressionElement)
            val expressionOffset = ParadoxExpressionManager.getExpressionOffset(expressionElement)
            val oldMode = ParadoxDefinitionInjectionManager.getModeFromExpression(expressionText) ?: return
            val range = TextRange.from(expressionOffset, oldMode.length)
            ElementManipulators.handleContentChange(expressionElement, range, mode)
        }
    }
}
