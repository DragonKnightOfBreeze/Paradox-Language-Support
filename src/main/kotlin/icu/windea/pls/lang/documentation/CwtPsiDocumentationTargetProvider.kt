package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.psi.mock.*

class CwtPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val elementWithDocumentation = element.navigationElement ?: element
        if (!isValid(elementWithDocumentation)) return null
        return CwtDocumentationTarget(elementWithDocumentation, originalElement)
    }

    private fun isValid(elementWithDocumentation: PsiElement): Boolean {
        if (elementWithDocumentation.language is CwtLanguage) return true
        if (elementWithDocumentation is CwtMockPsiElement) return true
        return false
    }
}
