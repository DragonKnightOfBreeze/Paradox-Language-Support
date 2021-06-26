package icu.windea.pls.cwt.expression

/**
 * 范围表达式。
 *
 * @property min 最小值
 * @property max 最大值，null表示无限
 * @property limitMax 如果值为`false`，则表示出现数量超出最大值时不警告
 */
class CwtCardinalityExpression (
	expression: String,
	val min: Int,
	val max: Int?,
	val limitMax: Boolean = true
) : AbstractExpression(expression) {
	companion object Resolver : AbstractExpressionResolver<CwtCardinalityExpression>() {
		override val emptyExpression = CwtCardinalityExpression("",0,null,true)
		
		override fun resolve(expression: String) = cache.getOrPut(expression) { doResolve(expression) }
		
		private fun doResolve(expression: String):CwtCardinalityExpression{
			return when {
				expression.isEmpty() -> {
					emptyExpression
				}
				expression.first() == '~' -> {
					val firstDotIndex = expression.indexOf('.')
					val min = expression.substring(1, firstDotIndex).toIntOrNull() ?: 0
					val max = expression.substring(firstDotIndex + 2)
						.let{ if(it.equals("inf",true)) null else it.toIntOrNull() ?: 0}
					val limitMax = true
					CwtCardinalityExpression(expression, min, max, limitMax)
				}
				else -> {
					val firstDotIndex = expression.indexOf('.')
					val min = expression.substring(0, firstDotIndex).toIntOrNull() ?: 0
					val max = expression.substring(firstDotIndex + 2)
						.let{ if(it.equals("inf",true)) null else it.toIntOrNull() ?: 0}
					val limitMax = false
					CwtCardinalityExpression(expression, min, max, limitMax)
				}
			}
		}
	}
	
	operator fun contains(value: Int) = value >= min && (max == null || value <= max)
	
	operator fun component1() = min
	
	operator fun component2() = max
	
	operator fun component3() = limitMax
}