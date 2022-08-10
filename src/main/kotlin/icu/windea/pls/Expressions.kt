package icu.windea.pls

import com.google.common.cache.*

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

interface ExpressionResolver<T : Expression> {
	fun resolve(expressionString: String): T
}

abstract class CachedExpressionResolver<T : Expression> : ExpressionResolver<T> {
	protected val cache: LoadingCache<String, T> by lazy { CacheBuilder.newBuilder().buildCache { doResolve(it) }}
	
	override fun resolve(expressionString: String): T {
		return cache.get(expressionString)
	}
	
	protected abstract fun doResolve(expressionString: String): T
}