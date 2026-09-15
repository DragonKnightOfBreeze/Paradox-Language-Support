package icu.windea.pls.script.breadcrumbs

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptElementPresentationService
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDirectValue

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
    private val _languages = arrayOf(ParadoxScriptLanguage)

    override fun getLanguages() = _languages

    override fun acceptElement(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxScriptProperty -> true
            is ParadoxScriptValue -> element.isDirectValue()
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptConditionalBlock -> true
            else -> false
        }
    }

    override fun getElementInfo(element: PsiElement): String {
        return ParadoxScriptElementPresentationService.getElementInfoInBreadCrumbs(element)
    }
}
