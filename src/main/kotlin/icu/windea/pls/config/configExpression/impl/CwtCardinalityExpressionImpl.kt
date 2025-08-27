package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.util.buildCache

internal class CwtCardinalityExpressionResolverImpl : CwtCardinalityExpression.Resolver {
    // 解析缓存：按原始字符串缓存解析结果
    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtCardinalityExpression> { doResolve(it) }
    // 空表达式（0..inf，且不宽松）复用
    private val emptyExpression = CwtCardinalityExpressionImpl("", 0, null, false, false)

    override fun resolveEmpty(): CwtCardinalityExpression = emptyExpression

    override fun resolve(expressionString: String): CwtCardinalityExpression {
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtCardinalityExpression {
        if (expressionString.isEmpty()) return emptyExpression
        // 以 ".." 分隔最小/最大值；缺失分隔符视为非法，回空
        val i = expressionString.indexOf("..")
        if (i == -1) return emptyExpression
        val s1 = expressionString.substring(0, i)
        val s2 = expressionString.substring(i + 2)
        // 支持 "~" 宽松标记；min 解析失败时回退为 0；max 不区分大小写的 "inf" 视为无限
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
