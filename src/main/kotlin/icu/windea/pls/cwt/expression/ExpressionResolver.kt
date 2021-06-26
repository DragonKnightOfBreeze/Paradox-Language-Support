package icu.windea.pls.cwt.expression

interface ExpressionResolver<T : Expression> {
	val emptyExpression:T
	
	fun resolve(expression: String): T
}