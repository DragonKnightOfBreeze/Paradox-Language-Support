package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys as Keys

class ParadoxScriptBasicAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)
        annotateParameterValue(element, holder)
    }

    private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
        //不允许紧邻的字面量
        if (element.isLiteral() && element.prevSibling.isLiteral()) {
            holder.newAnnotation(ERROR, PlsBundle.message("neighboring.literal.not.supported"))
                .withFix(InsertStringFix(PlsBundle.message("neighboring.literal.not.supported.fix"), " ", element.startOffset))
                .create()
        }
        //检测是否缺失一侧的双引号
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
