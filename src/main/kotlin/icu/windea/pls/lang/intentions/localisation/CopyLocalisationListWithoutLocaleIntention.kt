package icu.windea.pls.lang.intentions.localisation

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.PsiBasedModCommandAction
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 复制当前语言环境下的本地化列表到剪贴板（不保留语言环境前缀，保留其中的注释和空行）。
 */
class CopyLocalisationListWithoutLocaleIntention: PsiBasedModCommandAction<ParadoxLocalisationLocale>(ParadoxLocalisationLocale::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationListWithoutLocale")

    override fun perform(context: ActionContext, element: ParadoxLocalisationLocale): ModCommand {
        // 2.1.8 不检查 localeId 是否合法
        val localisationList = element.parent as? ParadoxLocalisationPropertyList ?: return ModCommand.nop()
        val text = localisationList.text.drop(element.textLength).trimIndent().trim() // 去除前缀，去除最小缩进，去除首尾空白
        return ModCommand.copyToClipboard(text)
    }
}
