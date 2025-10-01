package icu.windea.pls.cwt.navigation

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.cwt.CwtLanguage

class CwtBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(CwtLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return CwtNavigationManager.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return CwtNavigationManager.getLongPresentableText(element).orEmpty()
    }
}
