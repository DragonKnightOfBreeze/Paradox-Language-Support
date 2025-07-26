package icu.windea.pls.lang.references.csv

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.util.*

class ParadoxCsvExpressionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if(element !is ParadoxCsvExpressionElement) return PsiReference.EMPTY_ARRAY

        return ParadoxExpressionManager.getExpressionReferences(element)
    }
}
