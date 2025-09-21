package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 复制封装变量的本地化名称到剪贴板。
 */
abstract class CopyScriptedVariableLocalizedNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyScriptedVariableLocalizedName")

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
        return ParadoxScriptedVariableManager.getLocalizedName(element)?.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFinder.findScriptedVariable(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }
}
