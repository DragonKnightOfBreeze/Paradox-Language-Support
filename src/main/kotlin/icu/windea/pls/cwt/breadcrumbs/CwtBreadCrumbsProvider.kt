package icu.windea.pls.cwt.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtElementPresentationService
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isDirectValue

class CwtBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(CwtLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return when (element) {
            is CwtProperty -> true
            is CwtValue -> element.isDirectValue()
            else -> false
        }
    }

    override fun getElementInfo(element: PsiElement): String {
        return CwtElementPresentationService.getElementInfoInBreadCrumbs(element)
    }
}
