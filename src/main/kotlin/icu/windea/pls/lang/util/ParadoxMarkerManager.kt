package icu.windea.pls.lang.util

import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

object ParadoxMarkerManager {
    private const val matchedMarkers = "()[]{}<>"
    private val markerPairsL2R = matchedMarkers.chunked(2).associate { it.first() to it.last() }
    private val markerPairs = matchedMarkers.chunked(2).flatMap { listOf(it, it.reversed()) }.associate { it.first() to it.last() }

    fun isLeftMaker(marker: Char, expression: ParadoxComplexExpression? = null): Boolean {
        return when (expression) {
            null -> marker in "([{<"
            is ParadoxScopeFieldExpression, is ParadoxValueFieldExpression, is ParadoxVariableFieldExpression -> marker in "("
            is ParadoxCommandExpression -> marker in "("
            is ParadoxNameFormatExpression -> marker in "[{<"
            else -> false
        }
    }

    fun isLeftOrRightMaker(marker: Char, expression: ParadoxComplexExpression? = null): Boolean {
        return when (expression) {
            null -> marker in "()[]{}<>"
            is ParadoxScopeFieldExpression, is ParadoxValueFieldExpression, is ParadoxVariableFieldExpression -> marker in "()"
            is ParadoxCommandExpression -> marker in "()"
            is ParadoxNameFormatExpression -> marker in "[]{}<>"
            else -> false
        }
    }

    fun getMatchedMarkerFromLeft(leftMarker: Char): Char? {
        return markerPairsL2R[leftMarker]
    }

    fun getMatchedMarker(marker: Char): Char? {
        return markerPairs[marker]
    }

    fun getMatchedMarkerNode(node: ParadoxComplexExpressionNode): ParadoxComplexExpressionNode? {
        if (node !is ParadoxMarkerNode) return null
        val c = node.text.singleOrNull() ?: return null
        val s = markerPairs[c] ?: return null
        val nodes = node.parent?.nodes ?: return null
        return nodes.find { it is ParadoxMarkerNode && it.text.singleOrNull() == s && it !== node }
    }
}
