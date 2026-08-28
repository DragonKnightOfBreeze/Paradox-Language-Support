package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.lang.codeInsight.completion.script.ParadoxScriptParameterCompletionProvider
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter

/**
 * @see ParadoxScriptPsiReferenceProvider
 * @see ParadoxScriptParameterCompletionProvider
 */
class ParadoxScriptConditionParameterPsiReference(
    element: ParadoxScriptConditionParameter,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptConditionParameter>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    override fun resolve(): PsiElement? {
        return ParadoxParameterService.resolveConditionParameter(element)
    }
}
