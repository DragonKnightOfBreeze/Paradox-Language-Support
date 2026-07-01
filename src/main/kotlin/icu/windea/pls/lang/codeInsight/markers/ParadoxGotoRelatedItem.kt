package icu.windea.pls.lang.codeInsight.markers

import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvPsiPresentationService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiPresentationService
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptPsiPresentationService
import javax.swing.Icon

class ParadoxGotoRelatedItem(element: PsiElement, @NlsContexts.Separator group: String) : GotoRelatedItem(element, group) {
    override fun getCustomIcon(): Icon? {
        val element = element ?: return null
        return when (element.language) {
            ParadoxScriptLanguage -> ParadoxScriptPsiPresentationService.getPatchedIcon(element)
            ParadoxLocalisationLanguage -> ParadoxLocalisationPsiPresentationService.getPatchedIcon(element)
            ParadoxCsvLanguage -> ParadoxCsvPsiPresentationService.getPatchedIcon(element)
            else -> null
        }
    }

    override fun getCustomName(): String? {
        val element = element ?: return null
        return when (element.language) {
            ParadoxScriptLanguage -> ParadoxScriptPsiPresentationService.getPresentableText(element)
            ParadoxLocalisationLanguage -> ParadoxLocalisationPsiPresentationService.getPresentableText(element)
            ParadoxCsvLanguage -> ParadoxCsvPsiPresentationService.getPresentableText(element)
            else -> null
        }
    }

    override fun getCustomContainerName(): String? {
        // 使用相对于入口目录的路径，并且带上游戏信息/模组信息，或者使用虚拟文件的绝对路径
        val element = element ?: return null
        ParadoxPsiPresentationService.getLongFileInfoText(element)?.let { return it }
        return element.containingFile?.virtualFile?.path
    }
}
