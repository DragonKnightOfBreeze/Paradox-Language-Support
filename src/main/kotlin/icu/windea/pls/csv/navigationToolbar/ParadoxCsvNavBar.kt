package icu.windea.pls.csv.navigationToolbar

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvPsiPresentationService
import javax.swing.Icon

class ParadoxCsvNavBar : StructureAwareNavBarModelExtension() {
    override val language = ParadoxCsvLanguage

    override fun getIcon(o: Any?): Icon? {
        if (o !is PsiElement) return null
        return ParadoxCsvPsiPresentationService.getIcon(o)
    }

    override fun getPresentableText(o: Any?): String? {
        if (o !is PsiElement) return null
        return ParadoxCsvPsiPresentationService.getLongPresentableText(o)
    }
}
