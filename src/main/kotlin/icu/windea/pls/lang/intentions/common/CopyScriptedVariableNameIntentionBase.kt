package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 复制封装变量的名字到剪贴板。
 */
abstract class CopyScriptedVariableNameIntentionBase : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.copyScriptedVariableName")

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
        return element.name?.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptScriptedVariable? {
        val allOptions = ParadoxPsiManager.FindScriptedVariableOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findScriptVariable(context.file, context.offset, options)
    }
}
