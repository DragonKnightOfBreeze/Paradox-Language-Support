package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*

interface CwtExpression : Expression

abstract class CwtExpressionResolver<T : Expression> {
	private val cache: LoadingCache<String, T> by lazy { CacheBuilder.newBuilder().buildCache { doResolve(it) } }
	
	fun resolve(expressionString: String): T {
		return cache.get(expressionString)
	}
	
	protected abstract fun doResolve(expressionString: String): T
}