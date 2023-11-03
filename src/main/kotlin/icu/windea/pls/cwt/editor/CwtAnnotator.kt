package icu.windea.pls.cwt.editor

import com.intellij.lang.annotation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
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
		if(text.isLeftQuoted() && !text.isRightQuoted()) {
			//missing closing quote
			holder.newAnnotation(HighlightSeverity.ERROR, PlsBundle.message("syntax.error.missing.closing.quote")).create()
		}
	}
}