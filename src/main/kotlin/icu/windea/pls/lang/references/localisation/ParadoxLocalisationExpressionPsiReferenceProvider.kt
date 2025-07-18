package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationExpressionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxLocalisationExpressionElement) return PsiReference.EMPTY_ARRAY

        return ParadoxExpressionManager.getExpressionReferences(element)
    }
}

