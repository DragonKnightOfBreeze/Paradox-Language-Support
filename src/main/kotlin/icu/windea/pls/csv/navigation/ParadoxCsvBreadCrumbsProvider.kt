package icu.windea.pls.csv.navigation

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxCsvLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return ParadoxCsvNavigationManager.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxCsvNavigationManager.getLongPresentableText(element).orEmpty()
    }
}
