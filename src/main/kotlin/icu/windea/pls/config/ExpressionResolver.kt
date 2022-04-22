package icu.windea.pls.config

interface ExpressionResolver<T : Expression> {
	fun resolve(expressionString: String): T
}