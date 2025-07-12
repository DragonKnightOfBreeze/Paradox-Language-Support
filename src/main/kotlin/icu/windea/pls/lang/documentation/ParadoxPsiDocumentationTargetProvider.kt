package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*

class ParadoxPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val elementWithDocumentation = element.navigationElement ?: element
        if (!isValid(elementWithDocumentation)) return null
        return ParadoxDocumentationTarget(elementWithDocumentation, originalElement)
    }

    private fun isValid(elementWithDocumentation: PsiElement): Boolean {
        if (elementWithDocumentation.language is ParadoxBaseLanguage) return true
        if (elementWithDocumentation is ParadoxMockPsiElement) return true
        return false
    }
}
