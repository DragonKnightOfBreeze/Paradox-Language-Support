package icu.windea.pls.core.expression

interface Expression : CharSequence {
	val expressionString: String
}

abstract class AbstractExpression(override val expressionString: String) : Expression {
	override val length get() = expressionString.length
	
	override fun get(index: Int) = expressionString.get(index)
	
	override fun subSequence(startIndex: Int, endIndex: Int) = expressionString.subSequence(startIndex, endIndex)
	
	override fun equals(other: Any?) = other?.javaClass == javaClass && expressionString == (other as AbstractExpression).expressionString
	
	override fun hashCode() = expressionString.hashCode()
	
	override fun toString() = expressionString
}