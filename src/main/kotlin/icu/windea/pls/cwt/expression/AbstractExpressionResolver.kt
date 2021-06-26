package icu.windea.pls.cwt.expression

import java.util.concurrent.*

abstract class AbstractExpressionResolver<T : Expression> : ExpressionResolver<T> {
	protected val cache = ConcurrentHashMap<String, T>()
}