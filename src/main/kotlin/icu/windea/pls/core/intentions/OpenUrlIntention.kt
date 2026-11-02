package icu.windea.pls.core.intentions

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation

abstract class OpenUrlIntention : ModCommandAction {
    override fun getPresentation(context: ActionContext): Presentation? {
        getUrl(context) ?: return null
        return Presentation.of(getName(context))
    }

    override fun perform(context: ActionContext): ModCommand {
        val url = getUrl(context) ?: return ModCommand.nop()
        return ModCommand.openUrl(url)
    }

    protected open fun getName(context: ActionContext): String = familyName

    protected abstract fun getUrl(context: ActionContext): String?
}
