package icu.windea.pls.localisation.codeInsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.fixes.InsertStringFix
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 报告本地化文件中的语法错误并提供可用快速修复。
 *
 * 说明：
 * - 这些错误信息和快速修复由注解器（annotator）提供，而非由词法器（lexer）、解析器（parser）或代码检查（code inspection）提供。
 * - 这里的语法错误在词法级别和文法级别通常是被允许的。
 * - 相比代码检查，这里的报错无法禁用。
 */
class ParadoxLocalisationSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkQuote(element, holder)
        checkAdjacentIcon(element, holder)
    }

    private fun checkQuote(element: PsiElement, holder: AnnotationHolder) {
        // 检查是否缺失左侧或右侧的双引号
        if (element !is PsiQuoteAwareElement) return
        val isLeftQuoted = element.firstChild.elementType == ParadoxLocalisationElementTypes.LEFT_QUOTE
        val isRightQuoted = element.lastChild.elementType == ParadoxLocalisationElementTypes.RIGHT_QUOTE

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

    private fun checkAdjacentIcon(element: PsiElement, holder: AnnotationHolder) {
        // by @雪丶我
        // 不允许紧邻的图标
        if (element !is ParadoxLocalisationIcon) return
        if (element.prevSibling !is ParadoxLocalisationIcon) return
        val offset = element.startOffset
        holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.adjacent.icon.unexpected.message"))
            .range(TextRange.from(offset, 1))
            .withFix(InsertStringFix(element, ChronicleBundle.message("annotator.adjacent.icon.unexpected.fix"), " ", offset))
            .create()
    }
}
