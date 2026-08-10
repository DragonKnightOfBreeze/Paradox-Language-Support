package icu.windea.pls.lang.resolve.providers

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
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors

object ParadoxAnnotateProvider {
    fun annotateExpression(element: ParadoxExpressionElement, range: TextRange, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (range.isEmpty) return
        // skip parameter ranges
        val parameterRanges = ParadoxExpressionService.getParameterRangesInExpression(element)
        if (parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
        for (r in finalRanges) {
            if (r.isEmpty) continue
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r).textAttributes(attributesKey).create()
        }
    }

    fun annotateExpressionAsHighlightedReference(range: TextRange, holder: AnnotationHolder) {
        val attributesKey = DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE
        holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY).range(range).textAttributes(attributesKey).create()
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        annotateComplexExpressionNode(element, expression, holder, config)
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        if (node.text.isEmpty()) return

        val attributesKey = node.getAttributesKey(element)
        run {
            val mustUseAttributesKey = attributesKey != ParadoxScriptHighlighterColors.PROPERTY_KEY && attributesKey != ParadoxScriptHighlighterColors.STRING
            if (attributesKey != null && mustUseAttributesKey) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
                return@run
            }
            val attributesKeyConfig = node.getAttributesKeyConfig(element)
            if (attributesKeyConfig != null) {
                val offset = ParadoxExpressionService.getExpressionOffset(element)
                val rangeInElement = node.rangeInExpression.shiftRight(offset)
                val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
                ParadoxExpressionService.annotateScriptExpression(element, rangeInElement, expressionText, attributesKeyConfig, holder)
                return@run
            }
            if (attributesKey != null) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
            }
        }

        if (node.nodes.isNotEmpty()) {
            node.nodes.forEachFast { node ->
                annotateComplexExpressionNode(element, node, holder, config)
            }
        }
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (node.text.isEmpty()) return

        val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
        val rangeToAnnotate = node.rangeInExpression.shiftRight(offset)

        // merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        if (node is ParadoxTokenNode) {
            val editorColorsManager = EditorColorsManager.getInstance()
            val schema = editorColorsManager.activeVisibleScheme ?: editorColorsManager.schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpression(element, rangeToAnnotate, holder, attributesKey)
    }
}
