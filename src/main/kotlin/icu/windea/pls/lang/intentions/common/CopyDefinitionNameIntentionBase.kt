package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.*
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 复制定义的名字到剪贴板。
 */
abstract class CopyDefinitionNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyDefinitionName")

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
        return element.definitionInfo?.name?.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxScriptDefinitionElement? {
        val allOptions = ParadoxPsiManager.FindDefinitionOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findDefinition(context.file, context.offset, options)
    }
}
