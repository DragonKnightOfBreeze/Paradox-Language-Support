package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import icu.windea.pls.lang.*

class ParadoxPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val elementWithDocumentation = element.navigationElement ?: element
        if (!elementWithDocumentation.language.isParadoxLanguage()) return null
        return ParadoxDocumentationTarget(elementWithDocumentation, originalElement)
    }
}

