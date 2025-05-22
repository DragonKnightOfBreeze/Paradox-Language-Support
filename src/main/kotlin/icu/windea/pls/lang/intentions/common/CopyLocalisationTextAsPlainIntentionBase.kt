package icu.windea.pls.lang.intentions.common

import com.intellij.modcommand.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*

/**
 * 复制本地化文本到剪贴板。（复制的是处理后的纯文本）
 */
abstract class CopyLocalisationTextAsPlainIntentionBase : ModCommandAction {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationTextAsPlain")

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
        return ParadoxLocalisationTextRenderer.render(element)
    }

    private fun findElement(context: ActionContext): ParadoxLocalisationProperty? {
        val allOptions = ParadoxPsiManager.FindLocalisationOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findLocalisation(context.file, context.offset, options)
    }
}
