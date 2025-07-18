package icu.windea.pls.lang.references.csv

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager

class ParadoxCsvExpressionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if(element !is ParadoxCsvExpressionElement) return PsiReference.EMPTY_ARRAY

        return ParadoxExpressionManager.getExpressionReferences(element)
    }
}
