package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 复制封装变量的本地化名字到剪贴板。
 */
abstract class CopyScriptedVariableLocalizedNameIntentionBase : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.copyScriptedVariableLocalizedName")

    override fun getPresentation(context: ActionContext): Presentation? {
        val text = getText(context) ?: return null
        return Presentation.of(familyName)
    }

    override fun perform(context: ActionContext): ModCommand {
        val text = getText(context) ?: return ModCommand.nop()
        return ModCommand.copyToClipboard(text)
    }

    private fun getText(context: ActionContext): String? {
        val element = findElement(context) ?: return null
        val name = element.name?.orNull() ?: return null
        return ParadoxScriptedVariableManager.getHintFromExtendedConfig(name, context.file)?.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptScriptedVariable? {
        val allOptions = ParadoxPsiManager.FindScriptedVariableOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findScriptVariable(context.file, context.offset, options)
    }
}
