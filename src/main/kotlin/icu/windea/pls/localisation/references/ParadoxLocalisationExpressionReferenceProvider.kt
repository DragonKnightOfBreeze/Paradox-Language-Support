package icu.windea.pls.localisation.references

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationExpressionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxLocalisationExpressionElement) return PsiReference.EMPTY_ARRAY

        //尝试兼容可能包含参数的情况
        //if(text.isParameterized()) return PsiReference.EMPTY_ARRAY

        //尝试解析为complexExpression
        run {
            if (!element.isComplexExpression()) return@run
            val value = element.value
            val textRange = TextRange.create(0, value.length)
            val reference = ParadoxLocalisationExpressionPsiReference(element, textRange)
            return reference.collectReferences()
        }

        return PsiReference.EMPTY_ARRAY
    }
}
