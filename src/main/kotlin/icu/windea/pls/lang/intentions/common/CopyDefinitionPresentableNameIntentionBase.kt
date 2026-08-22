package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.util.ParadoxDefinitionManager

/**
 * 复制定义的展示名字到剪贴板。
 */
abstract class CopyDefinitionPresentableNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionPresentableName")

    override fun getPresentation(context: ActionContext): Presentation? {
        getText(context) ?: return null
        return Presentation.of(familyName)
    }

    override fun perform(context: ActionContext): ModCommand {
        val text = getText(context) ?: return ModCommand.nop()
        return ModCommand.copyToClipboard(text)
    }

    private fun getText(context: ActionContext): String? {
        val element = findElement(context) ?: return null
        // ParadoxHintTextProvider.getHintText(element)?.let { return it }
        return ParadoxDefinitionManager.getPresentableName(element)
    }

    private fun findElement(context: ActionContext): ParadoxDefinitionElement? {
        return ParadoxPsiFileService.findDefinition(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }
}

