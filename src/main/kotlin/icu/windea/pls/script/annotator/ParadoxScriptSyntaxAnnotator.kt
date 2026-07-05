package icu.windea.pls.script.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString

class ParadoxScriptSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkMissingQuote(element, holder)
        checkOperator(element, holder)
        checkInlineMathScriptedVariableReference(element, holder)
    }

    private fun checkMissingQuote(element: PsiElement, holder: AnnotationHolder) {
        // 检查是否缺失左侧或者右侧的双引号
        val quoteAware = element is ParadoxScriptPropertyKey || element is ParadoxScriptString
        if (!quoteAware) return
        val text = element.text

        // 检查是否缺失左侧或者右侧的双引号
        val isLeftQuoted = text.isLeftQuoted()
        val isRightQuoted = text.isRightQuoted()
        if (!isLeftQuoted && isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("message.missing.opening.quote")).create()
        } else if (isLeftQuoted && !isRightQuoted) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("message.missing.closing.quote")).create()
        }
    }

    private fun checkOperator(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.elementType ?: return
        if (elementType == ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) {
            // 2.1.10 #331 对于安全调用赋值运算符，不允许前导空白
            val leadingBlank = element.prevSibling?.takeIf { it.elementType == TokenType.WHITE_SPACE }
            if (leadingBlank != null) {
                holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("message.leading.blank.unexpected.1"))
                    .range(element)
                    .withFix(DeleteStringByElementTypeFix(leadingBlank, ChronicleBundle.message("fix.leading.blank.unexpected")))
                    .create()
            }
        }
    }

    private fun checkInlineMathScriptedVariableReference(element: PsiElement, holder: AnnotationHolder) {
        // 2.1.8 对于内联数学表达式中的封装变量引用，不需要也不允许前导的 `@`
        if (element !is ParadoxScriptInlineMathScriptedVariableReference) return
        val leadingAt = element.firstChild?.takeIf { it.elementType == ParadoxScriptElementTypes.AT }
        if (leadingAt != null) {
            holder.newAnnotation(HighlightSeverity.ERROR, ChronicleBundle.message("message.leading.at.unexpected.1"))
                .range(leadingAt)
                .withFix(DeleteStringByElementTypeFix(leadingAt, ChronicleBundle.message("fix.leading.at.unexpected")))
                .create()
        }
    }
}
