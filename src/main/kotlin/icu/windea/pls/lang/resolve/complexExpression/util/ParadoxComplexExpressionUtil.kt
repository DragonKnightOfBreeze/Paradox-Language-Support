package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.util.BitUtil
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionAttributes as Attributes

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

    inline fun checkAttribute(attributes: Int, provider: Attributes.() -> Int): Boolean {
        return BitUtil.isSet(attributes, Attributes.provider())
    }

    fun getAttributes(node: ParadoxComplexExpressionNode): Int {
        var r = 0
        node.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (isDynamicDataAware(node)) {
                    r = r or Attributes.DYNAMIC_DATA_AWARE
                    if (isPureDynamicDataAware(node)) {
                        r = r or Attributes.PURE_DYNAMIC_DATA_AWARE
                    }
                }

                return super.visit(node)
            }
        })
        return r
    }

    private fun isDynamicDataAware(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`

        return node is ParadoxDynamicDataNode
    }

    private fun isPureDynamicDataAware(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`
        // -parent -> `ParadoxLinkValueNode` (single child node)
        // --parent -> `ParadoxLinkNode` (last one)

        // if(node !is ParadoxDynamicDataNode) return false // unnecessary
        val parent1 = node.parent?.castOrNull<ParadoxLinkValueNode>() ?: return false
        if (parent1.nodes.size != 1) return false
        val parent2 = parent1.parent?.castOrNull<ParadoxLinkNode>() ?: return false
        if (parent2.nodes.last() != parent1) return false
        return true
    }
}
