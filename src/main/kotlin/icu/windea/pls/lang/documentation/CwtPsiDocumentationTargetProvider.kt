package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.psi.mock.CwtConfigMockPsiElement

class CwtPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val elementWithDocumentation = element.navigationElement ?: element
        if (!isValid(elementWithDocumentation)) return null
        return CwtDocumentationTarget(elementWithDocumentation, originalElement)
    }

    private fun isValid(elementWithDocumentation: PsiElement): Boolean {
        if (elementWithDocumentation.language is CwtLanguage) return true
        if (elementWithDocumentation is CwtConfigMockPsiElement) return true
        return false
    }
}
