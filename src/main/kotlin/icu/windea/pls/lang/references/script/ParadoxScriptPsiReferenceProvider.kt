package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter

class ParadoxScriptPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        val reference = when (element) {
            is ParadoxParameter -> getReference(element)
            is ParadoxScriptConditionParameter -> getReference(element)
            is ParadoxScriptedVariableReference -> getReference(element)
            else -> null
        }
        if (reference == null) return PsiReference.EMPTY_ARRAY
        return arrayOf(reference)
    }

    private fun getReference(element: ParadoxParameter): ParadoxParameterPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxParameterPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxScriptConditionParameter): ParadoxConditionParameterPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxConditionParameterPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxScriptedVariablePsiReference(element, rangeInElement)
    }
}
