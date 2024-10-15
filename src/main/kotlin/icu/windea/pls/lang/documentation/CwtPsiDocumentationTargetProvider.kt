package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*

class CwtPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val elementWithDocumentation = element.navigationElement ?: element
        if (elementWithDocumentation.language != CwtLanguage) return null
        return CwtDocumentationTarget(elementWithDocumentation, originalElement)
    }
}
