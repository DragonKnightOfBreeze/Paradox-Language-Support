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
 */
interface CwtCardinalityExpression : CwtExpression {
    val min: Int
    val max: Int?
    val relaxMin: Boolean

    operator fun component1() = min
    operator fun component2() = max
    operator fun component3() = relaxMin

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

private fun doResolveEmpty() = CwtCardinalityExpressionImpl("", 0, null, true)

private fun doResolve(expressionString: String): CwtCardinalityExpression {
    if (expressionString.isEmpty()) return doResolveEmpty()
    return when {
        expressionString.first() == '~' -> {
            val firstDotIndex = expressionString.indexOf('.')
            val min = expressionString.substring(1, firstDotIndex).toIntOrNull() ?: 0
            val max = expressionString.substring(firstDotIndex + 2)
                .let { if (it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
            val relaxMin = true
            CwtCardinalityExpressionImpl(expressionString.intern(), min, max, relaxMin)
        }
        else -> {
            val firstDotIndex = expressionString.indexOf('.')
            val min = expressionString.substring(0, firstDotIndex).toIntOrNull() ?: 0
            val max = expressionString.substring(firstDotIndex + 2)
                .let { if (it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
            val relaxMin = false
            CwtCardinalityExpressionImpl(expressionString, min, max, relaxMin)
        }
    }
}

private class CwtCardinalityExpressionImpl(
    override val expressionString: String,
    override val min: Int,
    override val max: Int?,
    override val relaxMin: Boolean
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
