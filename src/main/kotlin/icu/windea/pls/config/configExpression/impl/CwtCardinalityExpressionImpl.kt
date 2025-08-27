package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.util.buildCache

internal class CwtCardinalityExpressionResolverImpl : CwtCardinalityExpression.Resolver {
    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtCardinalityExpression> { doResolve(it) }
    private val emptyExpression = CwtCardinalityExpressionImpl("", 0, null, false, false)

    override fun resolveEmpty(): CwtCardinalityExpression = emptyExpression

    override fun resolve(expressionString: String): CwtCardinalityExpression {
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtCardinalityExpression {
        if (expressionString.isEmpty()) return emptyExpression
        val i = expressionString.indexOf("..")
        if (i == -1) return emptyExpression
        val s1 = expressionString.substring(0, i)
        val s2 = expressionString.substring(i + 2)
        val min = s1.removePrefix("~").let { n -> n.toIntOrNull() ?: 0 }
        val max = s2.removePrefix("~").let { n -> if (n.equals("inf", true)) null else n.toIntOrNull() }
        val relaxMin = s1.startsWith('~')
        val relaxMax = s2.startsWith('~')
        return CwtCardinalityExpressionImpl(expressionString, min, max, relaxMin, relaxMax)
    }
}

private class CwtCardinalityExpressionImpl(
    override val expressionString: String,
    override val min: Int,
    override val max: Int?,
    override val relaxMin: Boolean,
    override val relaxMax: Boolean
) : CwtCardinalityExpression {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtCardinalityExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
