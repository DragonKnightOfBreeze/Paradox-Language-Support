package icu.windea.pls.script.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptPsiPresentationService

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxScriptLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return ParadoxScriptPsiPresentationService.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxScriptPsiPresentationService.getLongPresentableText(element).orEmpty()
    }
}
