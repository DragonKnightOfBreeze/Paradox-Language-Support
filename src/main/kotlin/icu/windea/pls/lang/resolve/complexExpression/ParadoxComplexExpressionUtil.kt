package icu.windea.pls.lang.resolve.complexExpression

import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode

object ParadoxComplexExpressionUtil {
    private const val matchedMarkers = "()<>{}[]"
    private val markerPairsL2R = matchedMarkers.chunked(2).associate { it.take(1) to it.takeLast(1) }
    private val markerPairs = matchedMarkers.chunked(2).flatMap { listOf(it, it.reversed()) }.associate { it.take(1) to it.takeLast(1) }

    @Suppress("unused")
    fun isLeftMarkerNode(node: ParadoxComplexExpressionNode): Boolean {
        if (node !is ParadoxMarkerNode) return false
        return node.text in markerPairsL2R
    }

    fun getMatchedMarkerNode(node: ParadoxComplexExpressionNode): ParadoxComplexExpressionNode? {
        if (node !is ParadoxMarkerNode) return null
        val s = markerPairs[node.text] ?: return null
        val nodes = node.parent?.nodes ?: return null
        return nodes.find { it is ParadoxMarkerNode && it.text == s }
    }
}
