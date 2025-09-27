package icu.windea.pls.localisation.navigation

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxLocalisationLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return ParadoxLocalisationNavigationManager.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxLocalisationNavigationManager.getLongPresentableText(element).orEmpty()
    }
}
