package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 复制封装变量的展示名字到剪贴板。
 */
abstract class CopyScriptedVariablePresentableNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = ChronicleBundle.message("intention.copyScriptedVariablePresentableName")

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
        return ParadoxScriptedVariableManager.getPresentableName(element)?.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFileService.findScriptedVariable(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }
}
