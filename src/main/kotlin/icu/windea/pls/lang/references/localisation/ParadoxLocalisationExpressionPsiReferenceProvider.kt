package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

class ParadoxLocalisationExpressionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxLocalisationExpressionElement) return PsiReference.EMPTY_ARRAY

        return ParadoxExpressionManager.getExpressionReferences(element)
    }
}
