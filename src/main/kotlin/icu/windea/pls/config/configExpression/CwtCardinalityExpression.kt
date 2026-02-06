package icu.windea.pls.config.configExpression

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.core.cache.CacheBuilder

/**
 * 基数表达式。
 *
 * 用于约束定义成员的出现次数，驱动代码检查、代码补全等功能。
 * 支持宽松校验与无限上限。
 *
 * 用 `min..max` 表示允许的出现次数范围，`~` 为宽松标记，`inf` 表示无限。
 *
 * 适用对象：`## cardinality` 选项的值。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * ## cardinality = 0..1
 * ## cardinality = 0..inf
 * ## cardinality = ~1..10
 * ```
 *
 * @property min 最小值。
 * @property max 最大值，`null` 表示无限。
 * @property relaxMin 宽松标记。当为 `true` 时，小于最小值仅视为（弱）警告而非错误。
 * @property relaxMax 宽松标记。当为 `true` 时，大于最大值仅视为（弱）警告而非错误。
 *
 * @see CwtOptionDataHolder.cardinality
 */
interface CwtCardinalityExpression : CwtConfigExpression {
    val min: Int
    val max: Int?
    val relaxMin: Boolean
    val relaxMax: Boolean

    operator fun component1() = min
    operator fun component2() = max
    operator fun component3() = relaxMin
    operator fun component4() = relaxMax

    fun isRequired() = !relaxMin && min > 0

    interface Resolver {
        fun resolveEmpty(): CwtCardinalityExpression
        fun resolve(expressionString: String): CwtCardinalityExpression
    }

    companion object : Resolver by CwtCardinalityExpressionResolverImpl()
}

// region Implementations

private class CwtCardinalityExpressionResolverImpl : CwtCardinalityExpression.Resolver {
    private val logger = thisLogger()
    private val cache = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtCardinalityExpression> { key -> doResolve(key) }

    private val emptyExpression = CwtCardinalityExpressionImpl("", 0, null, false, false)

    override fun resolveEmpty(): CwtCardinalityExpression = emptyExpression

    override fun resolve(expressionString: String): CwtCardinalityExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtCardinalityExpression {
        // 以 ".." 分隔最小/最大值；缺失分隔符视为非法，回空
        val i = expressionString.indexOf("..")
        if (i == -1) return emptyExpression
        val s1 = expressionString.substring(0, i)
        val s2 = expressionString.substring(i + 2)
        // 支持 "~" 宽松标记；min 解析失败时回退为 0；max 不区分大小写的 "inf" 视为无限
        val min = s1.removePrefix("~").let { n -> n.toIntOrNull()?.coerceAtLeast(0) ?: 0 }
        val max = s2.removePrefix("~").let { n -> if (n.equals("inf", true)) null else n.toIntOrNull()?.coerceAtLeast(0) }
        if (max != null && min > max) {
            logger.warn("Invalid cardinality expression $expressionString, fallback to default")
            return emptyExpression
        }
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
    override fun equals(other: Any?) = this === other || other is CwtCardinalityExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion
