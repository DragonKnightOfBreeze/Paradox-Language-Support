package icu.windea.pls.script.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.ERROR
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.quickfix.InsertStringFix
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys as Keys

class ParadoxScriptBasicAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)
        annotateParameterValue(element, holder)
    }

    private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
        // TODO 2.0.2+ 澄清：由于 ParadoxScriptLexer 中会对 STRING_TOKEN 等进行合并，这里并不能捕捉到（计划以后重构，目前不视为语法性错误）
        //不允许紧邻的字面量
        if (element.isLiteral() && element.prevSibling.isLiteral()) {
            holder.newAnnotation(ERROR, PlsBundle.message("neighboring.literal.not.supported"))
                .withFix(InsertStringFix(PlsBundle.message("neighboring.literal.not.supported.fix"), " ", element.startOffset))
                .create()
        }

        // TODO 2.0.2+ 澄清：由于 ParadoxScriptLexer 中会对 STRING_TOKEN 等进行合并，这里的代码并不能起效（计划以后重构，目前不视为语法性错误）
        // 针对字符串内的特殊情况：如 a"b 被解析为同一个字符串（多个 STRING_TOKEN 片段）
        // 需要在第一个以右引号结尾但缺失左引号的片段上标记“缺失开引号”，并在紧随其后的片段上标记“紧邻字面量”
        // if (element is ParadoxScriptString) {
        //     val parts = PsiTreeUtil.findChildrenOfType(element, PsiElement::class.java)
        //         .filter { it.elementType == ParadoxScriptElementTypes.STRING_TOKEN }
        //         .sortedBy { it.textRange.startOffset }
        //     if (parts.size >= 2) {
        //         val idx = parts.indexOfFirst { !it.text.isLeftQuoted() && it.text.isRightQuoted() }
        //         if (idx != -1 && idx + 1 < parts.size) {
        //             // 缺失开引号：作用于以右引号结尾但未以左引号开始的片段（例如 a"）
        //             holder.newAnnotation(ERROR, PlsBundle.message("missing.opening.quote"))
        //                 .range(parts[idx])
        //                 .create()
        //             // 紧邻字面量：作用于紧随其后的下一个片段（例如 b）
        //             val nextPart = parts[idx + 1]
        //             holder.newAnnotation(ERROR, PlsBundle.message("neighboring.literal.not.supported"))
        //                 .range(nextPart)
        //                 .withFix(InsertStringFix(PlsBundle.message("neighboring.literal.not.supported.fix"), " ", nextPart.startOffset))
        //                 .create()
        //             return
        //         }
        //     }
        // }

        // 检测是否缺失一侧的双引号
        if (element.isQuoteAware()) {
            val text = element.text
            val isLeftQuoted = text.isLeftQuoted()
            val isRightQuoted = text.isRightQuoted()
            if (!isLeftQuoted && isRightQuoted) {
                holder.newAnnotation(ERROR, PlsBundle.message("missing.opening.quote")).create()
            } else if (isLeftQuoted && !isRightQuoted) {
                holder.newAnnotation(ERROR, PlsBundle.message("missing.closing.quote")).create()
            }
        }
    }

    private fun PsiElement?.isLiteral() = this is ParadoxScriptExpressionElement

    private fun PsiElement?.isQuoteAware() = this is ParadoxScriptStringExpressionElement

    private fun annotateParameterValue(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.elementType
        if (elementType != ParadoxScriptElementTypes.ARGUMENT_TOKEN) return
        val templateElement = element.parent?.parent ?: return
        val attributesKey = when {
            element.text.startsWith("@") -> Keys.SCRIPTED_VARIABLE_KEY
            templateElement is ParadoxScriptPropertyKey -> Keys.PROPERTY_KEY_KEY
            templateElement is ParadoxScriptString -> Keys.STRING_KEY
            templateElement is ParadoxScriptScriptedVariableName -> Keys.SCRIPTED_VARIABLE_KEY
            templateElement is ParadoxScriptScriptedVariableReference -> Keys.SCRIPTED_VARIABLE_KEY
            else -> return
        }
        holder.newSilentAnnotation(INFORMATION).range(element)
            .textAttributes(attributesKey)
            .create()
    }
}
