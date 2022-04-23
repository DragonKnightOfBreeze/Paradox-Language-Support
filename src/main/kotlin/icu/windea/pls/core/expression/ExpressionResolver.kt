package icu.windea.pls.core.expression

interface ExpressionResolver<T : Expression> {
	fun resolve(expressionString: String): T
}