package icu.windea.pls.cwt.navigationToolbar

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtPsiPresentationService
import javax.swing.Icon

class CwtNavBar : StructureAwareNavBarModelExtension() {
    override val language = CwtLanguage

    override fun getIcon(o: Any?): Icon? {
        if (o !is PsiElement) return null
        return CwtPsiPresentationService.getIcon(o)
    }

    override fun getPresentableText(o: Any?): String? {
        if (o !is PsiElement) return null
        return CwtPsiPresentationService.getLongPresentableText(o)
    }
}
