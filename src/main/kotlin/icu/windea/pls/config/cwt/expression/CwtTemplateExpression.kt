package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*

open class CwtTemplateExpression(
	expressionString: String,
	val snippetExpressions: List<CwtDataExpression>
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver {
		val EmptyExpression = CwtTemplateExpression("", emptyList())
		
		private val regex = """(\w+|<\w+>)+""".toRegex()
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) } }

		// job_<job>_add
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyExpression
			else -> {
				val matchResult = regex.matchEntire(expressionString)
				val snippets = matchResult?.groupValues?.drop(1).orEmpty()
				if(snippets.isEmpty()) EmptyExpression
				else {
					val snippetExpressions = snippets.map { CwtValueExpression.resolve(it) }
					CwtTemplateExpression(expressionString, snippetExpressions)
				}
			}
		}
	}
}

