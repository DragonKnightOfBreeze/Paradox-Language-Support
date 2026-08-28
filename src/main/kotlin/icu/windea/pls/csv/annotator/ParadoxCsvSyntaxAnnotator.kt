package icu.windea.pls.csv.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.fixes.InsertStringFix
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.psi.PsiQuoteAwareElement

class ParadoxCsvSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkQuote(element, holder)
    }

    private fun checkQuote(element: PsiElement, holder: AnnotationHolder) {
        // 检查是否缺失左侧或右侧的双引号
        // TODO 3.0.2 改为直接检查对应的词元（`LEFT_QUOTE` `RIGHT_QUOTE`）是否存在，而非检查文本是否用引号括起
        if (element !is PsiQuoteAwareElement) return
        val text = element.text
        val quotePattern = element.quotePattern
        val quote = quotePattern.quoteChar.toString()
        val isLeftQuoted = text.isLeftQuoted(quotePattern)
        val isRightQuoted = text.isRightQuoted(quotePattern)
        if (!isLeftQuoted && isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.missing.opening.quote.message"))
                .withFix(InsertStringFix(element, ChronicleBundle.message("annotator.missing.opening.quote.fix"), quote, element.startOffset))
                .create()
        } else if (isLeftQuoted && !isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.missing.closing.quote.message"))
                .withFix(InsertStringFix(element, ChronicleBundle.message("annotator.missing.closing.quote.fix"), quote, element.endOffset))
                .create()
        }
    }
}
