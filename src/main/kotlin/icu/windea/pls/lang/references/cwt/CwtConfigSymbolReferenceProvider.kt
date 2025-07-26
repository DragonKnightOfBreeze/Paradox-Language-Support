package icu.windea.pls.lang.references.cwt

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.util.*
import icu.windea.pls.cwt.psi.*

class CwtConfigSymbolReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference?> {
        ProgressManager.checkCanceled()

        if (element !is CwtStringExpressionElement) return PsiReference.EMPTY_ARRAY

        return CwtConfigSymbolManager.getReferences(element)
    }
}
