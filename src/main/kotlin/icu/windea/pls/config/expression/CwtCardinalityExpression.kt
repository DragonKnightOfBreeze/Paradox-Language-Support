package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.util.*

/**
 * CWT基数表达式。
 *
 * 示例：
 *
 * ```
 * ## cardinality = 0..1
 * ## cardinality = 0..inf
 * ## cardinality = ~1..10
 * ```
 *
 * @property min 最小值。
 * @property max 最大值，null表示无限。
 * @property relaxMin 如果值为`false`，则当实际数量小于最小值时仅会作出（弱）警告。
 * @property relaxMax 如果值为`false`，则当实际数量大于最大值时仅会作出（弱）警告。
 */
interface CwtCardinalityExpression : CwtExpression {
    val min: Int
    val max: Int?
    val relaxMin: Boolean
    val relaxMax: Boolean

    operator fun component1() = min
    operator fun component2() = max
    operator fun component3() = relaxMin
    operator fun component4() = relaxMax

    fun isOptional() = min == 0
    fun isRequired() = min > 0

    companion object Resolver {
        val EmptyExpression: CwtCardinalityExpression = doResolveEmpty()

        fun resolve(expressionString: String): CwtCardinalityExpression {
            if (expressionString.isEmpty()) return EmptyExpression
            return cache.get(expressionString)
        }
    }
}

//Implementations (cached & not interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtCardinalityExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtCardinalityExpressionImpl("", 0, null, false, false)

private fun doResolve(expressionString: String): CwtCardinalityExpression {
    if (expressionString.isEmpty()) return doResolveEmpty()
    val i = expressionString.indexOf("..")
    if (i == -1) return doResolveEmpty()
    val s1 = expressionString.substring(0, i)
    val s2 = expressionString.substring(i + 2)
    val min = s1.removePrefix("~").toIntOrNull() ?: 0
    val max = s2.removePrefix("~").toIntOrNull() ?: 0
    val relaxMin = s1.startsWith('~')
    val relaxMax = s2.startsWith('~')
    return CwtCardinalityExpressionImpl(expressionString, min, max, relaxMin, relaxMax)
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

    override fun hashCode(): Int {
        return expressionString.hashCode()
    }

    override fun toString(): String {
        return expressionString
    }
}
