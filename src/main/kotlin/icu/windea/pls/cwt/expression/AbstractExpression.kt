package icu.windea.pls.cwt.expression

abstract class AbstractExpression(override val expression: String) : Expression {
	override val length get() = expression.length
	
	override fun get(index: Int) = expression.get(index)
	
	override fun subSequence(startIndex: Int, endIndex: Int) = expression.subSequence(startIndex, endIndex)
	
	override fun equals(other: Any?) = other?.javaClass == javaClass && expression == (other as Expression).expression
	
	override fun hashCode() = expression.hashCode()
	
	override fun toString() = expression
}