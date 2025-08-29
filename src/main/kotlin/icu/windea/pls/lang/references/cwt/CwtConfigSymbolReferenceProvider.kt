package icu.windea.pls.lang.references.cwt

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.util.CwtConfigSymbolManager
import icu.windea.pls.cwt.psi.CwtStringExpressionElement

class CwtConfigSymbolReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference?> {
        ProgressManager.checkCanceled()

        if (element !is CwtStringExpressionElement) return PsiReference.EMPTY_ARRAY

        return CwtConfigSymbolManager.getReferences(element)
    }
}
