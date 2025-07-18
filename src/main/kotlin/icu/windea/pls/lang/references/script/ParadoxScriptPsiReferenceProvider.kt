package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        val reference = when (element) {
            is ParadoxScriptScriptedVariableReference -> getReference(element)
            is ParadoxScriptInlineMathScriptedVariableReference -> getReference(element)
            else -> null
        }
        if (reference == null) return PsiReference.EMPTY_ARRAY
        return arrayOf(reference)
    }

    private fun getReference(element: ParadoxScriptScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxScriptedVariablePsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxScriptInlineMathScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxScriptedVariablePsiReference(element, rangeInElement)
    }
}
