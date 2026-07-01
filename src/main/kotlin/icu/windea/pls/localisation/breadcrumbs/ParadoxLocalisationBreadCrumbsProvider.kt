package icu.windea.pls.localisation.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiPresentationService

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxLocalisationLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return ParadoxLocalisationPsiPresentationService.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxLocalisationPsiPresentationService.getLongPresentableText(element).orEmpty()
    }
}
