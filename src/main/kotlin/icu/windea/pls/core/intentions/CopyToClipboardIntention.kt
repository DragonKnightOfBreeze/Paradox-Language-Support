package icu.windea.pls.core.intentions

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation

abstract class CopyToClipboardIntention : ModCommandAction {
    override fun getPresentation(context: ActionContext): Presentation? {
        getText(context) ?: return null
        return Presentation.of(getName(context))
    }

    override fun perform(context: ActionContext): ModCommand {
        val text = getText(context) ?: return ModCommand.nop()
        return ModCommand.copyToClipboard(text)
    }

    open fun getName(context: ActionContext): String = familyName

    protected abstract fun getText(context: ActionContext): String?
}
