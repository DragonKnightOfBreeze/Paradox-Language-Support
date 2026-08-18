package icu.windea.pls.lang.resolve.util

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.startOffset
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors

object ParadoxAnnotateUtil {
    fun annotateExpression(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (rangeInExpression.isEmpty) return
        val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
        val range = rangeInExpression.shiftRight(offset)

        val parameterRanges = ParadoxExpressionService.getParameterRangesInExpression(element)
        if (parameterRanges.isNotEmpty()) {
            val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
            if (finalRanges is List<*> && finalRanges.isEmpty()) return
            finalRanges.forEach { finalRange ->
                if (finalRange.isEmpty) return@forEach
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(finalRange).textAttributes(attributesKey).create()
            }
            return
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
    }

    fun annotateExpressionAsHighlightedReference(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder) {
        if (rangeInExpression.isEmpty) return
        val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
        val range = rangeInExpression.shiftRight(offset)
        val attributesKey = DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE
        holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY).range(range).textAttributes(attributesKey).create()
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        annotateComplexExpressionNode(element, expression, holder, config)
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        if (node.text.isEmpty()) return

        run {
            val attributesKey = node.getAttributesKey(element)
            val mustUseAttributesKey = attributesKey != ParadoxScriptHighlighterColors.PROPERTY_KEY && attributesKey != ParadoxScriptHighlighterColors.STRING
            if (attributesKey != null && mustUseAttributesKey) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
                return@run
            }
            val attributesKeyConfig = node.getAttributesKeyConfig(element)
            if (attributesKeyConfig != null) {
                ParadoxExpressionService.annotateScriptExpression(element, node.text, node.rangeInExpression, attributesKeyConfig, holder)
                return@run
            }
            if (attributesKey != null) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
            }
        }

        node.nodes.forEachFast { node ->
            annotateComplexExpressionNode(element, node, holder, config)
        }
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (node.text.isEmpty()) return

        // merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        // do not apply this logic in tests
        if (node is ParadoxTokenNode && !ChronicleFacade.isUnitTestMode()) {
            val editorColorsManager = EditorColorsManager.getInstance()
            val schema = editorColorsManager.activeVisibleScheme ?: editorColorsManager.schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
            val range = node.rangeInExpression.shiftRight(offset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpression(element, node.rangeInExpression, holder, attributesKey)
    }
}
