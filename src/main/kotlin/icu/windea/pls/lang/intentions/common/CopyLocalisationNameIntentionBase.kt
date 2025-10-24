package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.Presentation
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 复制本地化的名字到剪贴板。
 */
abstract class CopyLocalisationNameIntentionBase : ModCommandAction, DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationName")

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
        return element.name.orNull()
    }

    private fun findElement(context: ActionContext): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisation(context.file, context.offset) { DEFAULT or BY_REFERENCE}
    }
}

