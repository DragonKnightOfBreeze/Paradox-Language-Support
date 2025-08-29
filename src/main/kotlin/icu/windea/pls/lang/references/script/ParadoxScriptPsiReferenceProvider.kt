package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference

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
