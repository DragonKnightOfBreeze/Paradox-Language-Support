package icu.windea.pls.lang.util

import com.intellij.util.BitUtil
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionAttributes
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionAttributesEvaluator

object ParadoxComplexExpressionManager {
    /**
     * @see ParadoxComplexExpressionAttributes
     * @see ParadoxComplexExpressionAttributesEvaluator
     */
    inline fun checkAttribute(attributes: Int, provider: ParadoxComplexExpressionAttributes.() -> Int): Boolean {
        return BitUtil.isSet(attributes, ParadoxComplexExpressionAttributes.provider())
    }

    /**
     * @see ParadoxComplexExpressionAttributes
     * @see ParadoxComplexExpressionAttributesEvaluator
     */
    fun getAttributes(node: ParadoxComplexExpressionNode): Int {
        return ParadoxComplexExpressionAttributesEvaluator.evaluate(node)
    }
}
