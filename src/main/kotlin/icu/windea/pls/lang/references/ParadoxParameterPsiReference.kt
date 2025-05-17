package icu.windea.pls.lang.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.script.psi.*

class ParadoxParameterPsiReference(
    element: ParadoxParameter,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxParameter>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    override fun resolve(): PsiElement? {
        return ParadoxParameterSupport.resolveParameter(element)
    }
}
