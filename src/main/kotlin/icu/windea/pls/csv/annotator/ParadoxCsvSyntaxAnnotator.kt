package icu.windea.pls.csv.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
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
        if (element !is PsiQuoteAwareElement) return
        val text = element.text
        val quotePattern = element.quotePattern
        val isLeftQuoted = text.isLeftQuoted(quotePattern)
        val isRightQuoted = text.isRightQuoted(quotePattern)

        // 可以完全未用引号包围
        if (!isLeftQuoted && !isRightQuoted) return

        val quote = element.quotePattern.quoteChar.toString()
        if (!isLeftQuoted) {
            val offset = element.startOffset
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.missing.opening.quote.message"))
                .range(TextRange.from(offset, 0))
                .withFix(InsertStringFix(element, ChronicleBundle.message("annotator.missing.opening.quote.fix"), quote, element.startOffset))
                .create()
        }
        if (!isRightQuoted) {
            val offset = element.endOffset
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.missing.closing.quote.message"))
                .range(TextRange.from(offset, 0))
                .withFix(InsertStringFix(element, ChronicleBundle.message("annotator.missing.closing.quote.fix"), quote, offset))
                .create()
        }
    }
}
