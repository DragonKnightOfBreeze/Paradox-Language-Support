package icu.windea.pls.script.codeInsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.core.fixes.InsertStringFix
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference

/**
 * 报告脚本文件中的语法错误并提供可用快速修复。
 *
 * 说明：
 * - 这些错误信息和快速修复由注解器（annotator）提供，而非由词法器（lexer）、解析器（parser）或代码检查（code inspection）提供。
 * - 这里的语法错误在词法级别和文法级别通常是被允许的。
 * - 相比代码检查，这里的报错无法禁用。
 */
class ParadoxScriptSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkQuote(element, holder)
        checkOperator(element, holder)
        checkInlineMathScriptedVariableReference(element, holder)
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

    private fun checkOperator(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.elementType ?: return
        if (elementType == ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) {
            // 2.1.10 #331 对于安全调用赋值运算符，不允许前导空白
            val leadingBlank = element.prevSibling?.takeIf { it.elementType == TokenType.WHITE_SPACE }
            if (leadingBlank != null) {
                holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.leading.blank.unexpected.message.1"))
                    .range(element)
                    .withFix(DeleteStringByElementTypeFix(leadingBlank, ChronicleBundle.message("annotator.leading.blank.unexpected.fix")))
                    .create()
            }
        }
    }

    private fun checkInlineMathScriptedVariableReference(element: PsiElement, holder: AnnotationHolder) {
        // 2.1.8 对于内联数学表达式中的封装变量引用，不需要也不允许前导的 `@`
        if (element !is ParadoxScriptInlineMathScriptedVariableReference) return
        val leadingAt = element.firstChild?.takeIf { it.elementType == ParadoxScriptElementTypes.AT }
        if (leadingAt != null) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("annotator.leading.at.unexpected.message.1"))
                .range(leadingAt)
                .withFix(DeleteStringByElementTypeFix(leadingAt, ChronicleBundle.message("annotator.leading.at.unexpected.fix")))
                .create()
        }
    }
}
