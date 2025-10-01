package icu.windea.pls.script.navigation

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxScriptLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return ParadoxScriptNavigationManager.accept(element, forFile = false)
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxScriptNavigationManager.getLongPresentableText(element).orEmpty()
    }
}
