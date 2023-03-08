package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.*

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
class CwtCardinalityExpression private constructor(
	expressionString: String,
	val min: Int,
	val max: Int?,
	val relaxMin: Boolean = false
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver {
		val EmptyExpression = CwtCardinalityExpression("", 0, null, true)
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtCardinalityExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyExpression
			expressionString.first() == '~' -> {
				val firstDotIndex = expressionString.indexOf('.')
				val min = expressionString.substring(1, firstDotIndex).toIntOrNull() ?: 0
				val max = expressionString.substring(firstDotIndex + 2)
					.let { if(it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
				val relaxMin = true
				CwtCardinalityExpression(expressionString, min, max, relaxMin)
			}
			else -> {
				val firstDotIndex = expressionString.indexOf('.')
				val min = expressionString.substring(0, firstDotIndex).toIntOrNull() ?: 0
				val max = expressionString.substring(firstDotIndex + 2)
					.let { if(it.equals("inf", true)) null else it.toIntOrNull() ?: 0 }
				val relaxMin = false
				CwtCardinalityExpression(expressionString, min, max, relaxMin)
			}
		}
	}
	
	operator fun component1() = min
	
	operator fun component2() = max
	
	operator fun component3() = relaxMin
	
	fun isOptional() = min == 0
	
	fun isRequired() = min > 0
}