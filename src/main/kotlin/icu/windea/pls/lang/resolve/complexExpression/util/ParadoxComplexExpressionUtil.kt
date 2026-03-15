package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode

object ParadoxComplexExpressionUtil {
    private const val matchedMarkers = "()<>{}[]"
    private val markerPairsL2R = matchedMarkers.chunked(2).associate { it.take(1) to it.takeLast(1) }
    private val markerPairs = matchedMarkers.chunked(2).flatMap { listOf(it, it.reversed()) }.associate { it.take(1) to it.takeLast(1) }

    fun isLeftMaker(marker: String, expression: ParadoxComplexExpression? = null): Boolean {
        return when (expression) {
            null -> marker in "{<[("
            is ParadoxScopeFieldExpression, is ParadoxValueFieldExpression, is ParadoxVariableFieldExpression -> marker == "("
            is ParadoxCommandExpression -> marker == "("
            is StellarisNameFormatExpression -> marker in "{<["
            else -> false
        }
    }

    fun isLeftOrRightMaker(marker: String, expression: ParadoxComplexExpression? = null): Boolean {
        return when (expression) {
            null -> marker in "{}<>[]()"
            is ParadoxScopeFieldExpression, is ParadoxValueFieldExpression, is ParadoxVariableFieldExpression -> marker == "()"
            is ParadoxCommandExpression -> marker == "()"
            is StellarisNameFormatExpression -> marker in "{}<>[]"
            else -> false
        }
    }

    fun getMatchedMarker(leftMarker: String): String? {
        return markerPairsL2R[leftMarker]
    }

    fun getMatchedMarkerNode(node: ParadoxComplexExpressionNode): ParadoxComplexExpressionNode? {
        if (node !is ParadoxMarkerNode) return null
        val s = markerPairs[node.text] ?: return null
        val nodes = node.parent?.nodes ?: return null
        return nodes.find { it is ParadoxMarkerNode && it.text == s }
    }
}
