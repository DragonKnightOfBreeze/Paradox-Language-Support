package icu.windea.pls.cwt.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtPsiPresentationService

class CwtBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(CwtLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return CwtPsiPresentationService.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return CwtPsiPresentationService.getLongPresentableText(element).orEmpty()
    }
}
