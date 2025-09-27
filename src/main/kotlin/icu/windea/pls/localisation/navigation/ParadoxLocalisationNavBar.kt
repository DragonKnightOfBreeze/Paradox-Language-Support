package icu.windea.pls.localisation.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import javax.swing.Icon

class ParadoxLocalisationNavBar : StructureAwareNavBarModelExtension() {
    override val language = ParadoxLocalisationLanguage

    override fun getIcon(o: Any?): Icon? {
        if (o !is PsiElement) return null
        return ParadoxLocalisationNavigationManager.getIcon(o)
    }

    override fun getPresentableText(o: Any?): String? {
        if (o !is PsiElement) return null
        return ParadoxLocalisationNavigationManager.getLongPresentableText(o)
    }
}
