package icu.windea.pls.csv.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.csv.psi.ParadoxCsvColumn

class ParadoxCsvSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkQuote(element, holder)
    }

    private fun checkQuote(element: PsiElement, holder: AnnotationHolder) {
        val quoteAware = element is ParadoxCsvColumn
        if (!quoteAware) return
        val text = element.text

        // 检查是否缺失左侧或者右侧的双引号
        val isLeftQuoted = text.isLeftQuoted()
        val isRightQuoted = text.isRightQuoted()
        if (!isLeftQuoted && isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, PlsBundle.message("message.missing.opening.quote")).create()
        } else if (isLeftQuoted && !isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, PlsBundle.message("message.missing.closing.quote")).create()
        }
    }
}
