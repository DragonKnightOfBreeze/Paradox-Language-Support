package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 复制定义的本地化名称到剪贴板。
 */
abstract class CopyDefinitionLocalizedNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyDefinitionLocalizedName")

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
        return ParadoxDefinitionManager.getLocalizedNames(element).firstOrNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptDefinitionElement? {
        val allOptions = ParadoxPsiManager.FindDefinitionOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findDefinition(context.file, context.offset, options)
    }
}

