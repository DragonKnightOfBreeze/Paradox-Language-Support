package icu.windea.pls.cwt.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

class CwtAnnotator: Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when(element) {
			is CwtPropertyKey -> checkLiteralElement(element, holder) 
			is CwtString -> checkLiteralElement(element, holder) 
			is CwtOptionKey -> checkLiteralElement(element, holder) 
		}
	}
	
	private fun checkLiteralElement(element: PsiElement, holder: AnnotationHolder) {
		val text = element.text
		val isLeftQuoted = text.isLeftQuoted()
		val isRightQuoted = text.isRightQuoted()
		if(!isLeftQuoted && isRightQuoted) {
			holder.newAnnotation(ERROR, PlsBundle.message("syntax.error.missing.opening.quote")).range(element).create()
		} else if(isLeftQuoted && !isRightQuoted) {
			holder.newAnnotation(ERROR, PlsBundle.message("syntax.error.missing.closing.quote")).range(element).create()
		}
	}
}