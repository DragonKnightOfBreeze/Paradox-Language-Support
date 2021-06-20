package icu.windea.pls

import java.util.concurrent.*

interface Expression : CharSequence {
	val expression: String
}

abstract class AbstractExpression(override val expression: String) : Expression {
	override val length get() = expression.length
	
	override fun get(index: Int) = expression.get(index)
	
	override fun subSequence(startIndex: Int, endIndex: Int) = expression.subSequence(startIndex, endIndex)
	
	override fun equals(other: Any?) = other?.javaClass == javaClass && expression == (other as Expression).expression
	
	override fun hashCode() = expression.hashCode()
	
	override fun toString() = expression
}

interface ExpressionResolver<T : Expression> {
	fun resolve(expression: String): T
}

abstract class AbstractExpressionResolver<T : Expression> : ExpressionResolver<T> {
	protected val cache = ConcurrentHashMap<String, T>()
}

/**
 * 范围表达式。
 *
 * @property min 最小值
 * @property max 最大值，null表示无限
 * @property limitMax 如果值为`false`，则表示出现数量超出最大值时不警告
 */
class RangeExpression private constructor(expression: String) : AbstractExpression(expression) {
	companion object Resolver : AbstractExpressionResolver<RangeExpression>() {
		override fun resolve(expression: String) = cache.getOrPut(expression) { RangeExpression(expression) }
	}
	
	val min: Int
	val max: Int?
	val limitMax: Boolean
	
	init {
		when {
			expression.isEmpty() -> {
				min = 0
				max = null
				limitMax = false
			}
			expression.first() == '~' -> {
				val firstDotIndex = expression.indexOf('.')
				min = expression.substring(1, firstDotIndex).toIntOrNull() ?: 0
				max = expression.substring(firstDotIndex + 2).toIntOrNull() ?: 0
				limitMax = true
			}
			else -> {
				val firstDotIndex = expression.indexOf('.')
				min = expression.substring(0, firstDotIndex).toIntOrNull() ?: 0
				max = expression.substring(firstDotIndex + 2).toIntOrNull() ?: 0
				limitMax = false
			}
		}
	}
	
	operator fun contains(value: Int): Boolean {
		return value >= min && (max == null || value <= max)
	}
	
	operator fun component1() = min
	
	operator fun component2() = max
	
	operator fun component3() = limitMax
}