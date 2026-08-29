package icu.windea.pls.cwt.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.cwt.editor.CwtHighlighterColors
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtTokenSets

class CwtHighlightingAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        annotateQuote(element, holder)
    }

    private fun annotateQuote(element: PsiElement, holder: AnnotationHolder) {
        // 高亮未出现在字符串中的引号为匹配的语言高亮
        val elementType = element.elementType
        if (elementType !in CwtTokenSets.QUOTE_TOKENS) return
        val attributesKey = when (element.parent) {
            is CwtOptionKey -> CwtHighlighterColors.OPTION_KEY
            is CwtPropertyKey -> CwtHighlighterColors.PROPERTY_KEY
            else -> return
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(attributesKey).create()
    }
}
