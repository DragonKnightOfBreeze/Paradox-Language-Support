package icu.windea.pls.cwt.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.cwt.psi.*

class CwtAnnotator: Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		checkSyntax(element, holder)
	}
	
	private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
		//不允许紧邻的字面量
		if(element.isLiteral() && element.prevSibling.isLiteral()) {
			holder.newAnnotation(ERROR, PlsBundle.message("neighboring.literal.not.supported"))
				.withFix(InsertStringFix(PlsBundle.message("neighboring.literal.not.supported.fix"), " ", element.startOffset))
				.create()
		}
		//检测是否缺失一侧的双引号
		if(element.isQuoteAware()) {
			val text = element.text
			val isLeftQuoted = text.isLeftQuoted()
			val isRightQuoted = text.isRightQuoted()
			if(!isLeftQuoted && isRightQuoted) {
				holder.newAnnotation(ERROR, PlsBundle.message("missing.opening.quote")).create()
			} else if(isLeftQuoted && !isRightQuoted) {
				holder.newAnnotation(ERROR, PlsBundle.message("missing.closing.quote")).create()
			}
		}
	}
	
	private fun PsiElement?.isLiteral() = this is CwtPropertyKey || this is CwtValue
	
	private fun PsiElement?.isQuoteAware() = this is CwtOptionKey || this is CwtPropertyKey || this is CwtString
}