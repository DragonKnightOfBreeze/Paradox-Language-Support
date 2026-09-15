package icu.windea.pls.csv.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvElementPresentationService

class ParadoxCsvBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxCsvLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxCsvColumnContainer -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxCsvElementPresentationService.getElementInfoInBreadCrumbs(element)
    }
}
