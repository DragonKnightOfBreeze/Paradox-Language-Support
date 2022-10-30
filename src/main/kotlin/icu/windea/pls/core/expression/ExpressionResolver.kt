package icu.windea.pls.core.expression

abstract class ExpressionResolver<T : Expression> {
	open fun resolve(expressionString: String): T {
		return doResolve(expressionString)
	}
	
	protected abstract fun doResolve(expressionString: String): T
}

