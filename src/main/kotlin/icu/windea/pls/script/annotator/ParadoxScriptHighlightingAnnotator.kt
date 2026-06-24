package icu.windea.pls.script.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString

class ParadoxScriptHighlightingAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        annotateParameterValue(element, holder)
    }

    private fun annotateParameterValue(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.elementType
        if (elementType != ParadoxScriptElementTypes.ARGUMENT_TOKEN) return
        val parameterElement = element.parent?.parent as? ParadoxParameter ?: return
        val templateElement = parameterElement.parent ?: return
        when {
            element.text.startsWith('@') -> annotateRangeWithAtSign(holder, element, ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_REFERENCE)
            templateElement is ParadoxScriptPropertyKey -> annotateRange(holder, element, ParadoxScriptHighlighterColors.PROPERTY_KEY)
            templateElement is ParadoxScriptString -> annotateRange(holder, element, ParadoxScriptHighlighterColors.STRING)
            templateElement is ParadoxScriptScriptedVariableName -> annotateRangeWithAtSign(holder, element, ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_NAME)
            templateElement is ParadoxScriptScriptedVariableReference -> annotateRangeWithAtSign(holder, element, ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_REFERENCE)
        }
    }

    private fun annotateRange(holder: AnnotationHolder, element: PsiElement, attributesKey: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(attributesKey).create()
    }

    private fun annotateRangeWithAtSign(holder: AnnotationHolder, element: PsiElement, attributesKey: TextAttributesKey) {
        val range = element.textRange
        val rangeForAtSign = TextRange.from(range.startOffset, 1)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeForAtSign).textAttributes(ParadoxScriptHighlighterColors.AT_SIGN).create()
        val rangeForRemain = TextRange.create(range.startOffset + 1, range.endOffset)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeForRemain).textAttributes(attributesKey).create()
    }
}
