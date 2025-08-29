package icu.windea.pls.core.psi

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.ReferenceRange
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.util.Processor

abstract class FilteredRequestResultProcessor(private val target: PsiElement) : RequestResultProcessor(target) {
    override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
        val apply = applyFor(element)
        if (apply && !acceptElement(element)) return true
        if (!target.isValid) return false
        val references = PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
        for (i in references.indices) {
            val ref = references[i]
            if (apply && !acceptReference(ref)) continue
            ProgressManager.checkCanceled()
            if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && ref.isReferenceTo(target) && !consumer.process(ref)) {
                return false
            }
        }
        return true
    }

    protected open fun applyFor(element: PsiElement): Boolean = true

    protected open fun acceptElement(element: PsiElement): Boolean = true

    protected open fun acceptReference(reference: PsiReference): Boolean = true
}
