package icu.windea.pls.core.expression

import com.google.common.cache.*
import icu.windea.pls.*

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

abstract class ExpressionResolver<T : Expression> {
	open fun resolve(expressionString: String): T {
		return doResolve(expressionString)
	}
	
	protected abstract fun doResolve(expressionString: String): T
}

abstract class CachedExpressionResolver<T : Expression> : ExpressionResolver<T>() {
	protected val cache: LoadingCache<String, T> by lazy { CacheBuilder.newBuilder().buildCache { doResolve(it) } }
	
	override fun resolve(expressionString: String): T {
		return cache.get(expressionString)
	}
	
	abstract override fun doResolve(expressionString: String): T
}