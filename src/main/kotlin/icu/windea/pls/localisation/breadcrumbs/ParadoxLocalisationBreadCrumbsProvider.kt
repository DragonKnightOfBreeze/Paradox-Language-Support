package icu.windea.pls.localisation.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementPresentationService

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxLocalisationLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxLocalisationLocale -> true
            is ParadoxLocalisationPropertyList -> true
            is ParadoxLocalisationProperty -> true
            else -> false
        }
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxLocalisationElementPresentationService.getLongPresentableText(element).orEmpty()
    }
}
