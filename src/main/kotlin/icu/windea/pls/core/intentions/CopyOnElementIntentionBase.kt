package icu.windea.pls.core.intentions

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware

abstract class CopyOnElementIntentionBase: ModCommandAction, DumbAware {
    override fun getPresentation(context: ActionContext): Presentation? {
        getText(context) ?: return null
        return Presentation.of(familyName)
    }

    override fun perform(context: ActionContext): ModCommand {
        val text = getText(context) ?: return ModCommand.nop()
        return ModCommand.copyToClipboard(text)
    }

    protected abstract fun getText(context: ActionContext): String?
}
