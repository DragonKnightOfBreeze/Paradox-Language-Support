package icu.windea.pls.core.expression

import icu.windea.pls.*

abstract class CachedExpressionResolver<T : Expression> : ExpressionResolver<T> {
	protected val cache by lazy { createCache<String, T> { doResolve(it) } }
	
	override fun resolve(expressionString: String): T {
		return cache.get(expressionString)
	}
	
	abstract fun doResolve(expressionString: String): T
}