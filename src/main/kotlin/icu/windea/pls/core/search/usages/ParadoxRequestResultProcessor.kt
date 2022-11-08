package icu.windea.pls.core.search.usages

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*

class ParadoxRequestResultProcessor(
	private val target: PsiElement
) : RequestResultProcessor(target) {
	private val referenceService = PsiReferenceService.getService()
	
	override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
		if(!target.isValid) return false
		val references = referenceService.getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
		return references.all { reference ->
			ProgressManager.checkCanceled()
			if(ReferenceRange.containsOffsetInElement(reference, offsetInElement) && reference.isReferenceToTarget(target)){
				consumer.process(reference)
			} else {
				true
			}
		}
	}
	
	private fun PsiReference.isReferenceToTarget(element: PsiElement): Boolean {
		if(isReferenceTo(element)) return true
		//TODO
		return false
	}
}
