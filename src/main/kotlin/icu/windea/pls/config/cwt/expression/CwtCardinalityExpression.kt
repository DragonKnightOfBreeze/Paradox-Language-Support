package icu.windea.pls.config.cwt.expression

import icu.windea.pls.core.expression.*

/**
 * CWT基数表达式。
 *
 * 示例：`"0..1"`, `"0..inf"`, `"~0..10"`
 * @property min 最小值。
 * @property max 最大值，null表示无限。
 * @property limitMax 如果值为`false`，则表示出现数量超出最大值时不警告。
 */
class CwtCardinalityExpression private constructor(
	expressionString: String,
	val min: Int,
	val max: Int?,
	val limitMax: Boolean = true
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtCardinalityExpression>() {
		val EmptyExpression = CwtCardinalityExpression("", 0, null, true)
		
		override fun doResolve(expressionString: String): CwtCardinalityExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString.first() == '~' -> {
					val firstDotIndex = expressionString.indexOf('.')
					val min = expressionString.substring(1, firstDotIndex).toIntOrNull() ?: 0
					val max = expressionString.substring(firstDotIndex + 2)
						.let { if(it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
					val limitMax = true
					CwtCardinalityExpression(expressionString, min, max, limitMax)
				}
				else -> {
					val firstDotIndex = expressionString.indexOf('.')
					val min = expressionString.substring(0, firstDotIndex).toIntOrNull() ?: 0
					val max = expressionString.substring(firstDotIndex + 2)
						.let { if(it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
					val limitMax = false
					CwtCardinalityExpression(expressionString, min, max, limitMax)
				}
			}
		}
	}
	
	operator fun contains(value: Int) = value >= min && (max == null || value <= max)
	
	operator fun component1() = min
	
	operator fun component2() = max
	
	operator fun component3() = limitMax
}