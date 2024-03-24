package icu.windea.pls.lang.search

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*

private val ourReferenceService = PsiReferenceService.getService()
// icu.windea.pls.lang.search.FilteredRequestResultProcessor
abstract class FilteredRequestResultProcessor(private val target: PsiElement) : RequestResultProcessor(target) {
    override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
        val apply = applyFor(element)
        if(apply && !acceptElement(element)) return true
        if(!target.isValid) return false
        val references = ourReferenceService.getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
        for(i in references.indices) {
            val ref = references[i]
            if(apply && !acceptReference(ref)) continue
            ProgressManager.checkCanceled()
            if(ReferenceRange.containsOffsetInElement(ref, offsetInElement) && ref.isReferenceTo(target) && !consumer.process(ref)) {
                return false
            }
        }
        return true
    }
    
    protected open fun applyFor(element: PsiElement): Boolean = true
    
    protected open fun acceptElement(element: PsiElement): Boolean = true
    
    protected open fun acceptReference(reference: PsiReference): Boolean = true
}
