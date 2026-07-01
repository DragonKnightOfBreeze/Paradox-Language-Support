package icu.windea.pls.lang

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvPsiPresentationService
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiPresentationService
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptPsiPresentationService
import javax.swing.Icon

/**
 * 为一些特定的 PSI 元素提供特殊图标。
 */
class ParadoxIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        return when (element.language) {
            ParadoxScriptLanguage -> ParadoxScriptPsiPresentationService.getPatchedIcon(element)
            ParadoxLocalisationLanguage -> ParadoxLocalisationPsiPresentationService.getPatchedIcon(element)
            ParadoxCsvLanguage -> ParadoxCsvPsiPresentationService.getPatchedIcon(element)
            else -> null
        }
    }
}
