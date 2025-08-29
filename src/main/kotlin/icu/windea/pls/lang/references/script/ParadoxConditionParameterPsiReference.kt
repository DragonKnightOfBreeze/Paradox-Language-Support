package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.script.psi.ParadoxConditionParameter

class ParadoxConditionParameterPsiReference(
    element: ParadoxConditionParameter,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxConditionParameter>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    override fun resolve(): PsiElement? {
        return ParadoxParameterSupport.resolveConditionParameter(element)
    }
}
