package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*

open class CwtSimpleTemplateExpression(
	expressionString: String,
	val prefixSnippet: String,
	val suffixSnippet: String
): AbstractExpression(expressionString), CwtExpression {
	companion object Resolver {
		val EmptyExpression = CwtSimpleTemplateExpression("", "", "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtSimpleTemplateExpression> { doResolve(it) } }
		
		// $_desc
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyExpression
			else -> {
				val i = expressionString.indexOf('$')
				if(i == -1) CwtSimpleTemplateExpression(expressionString, expressionString, "")
				else {
					val prefix = expressionString.substring(0, i)
					val suffix = expressionString.substring(i + 1)
					CwtSimpleTemplateExpression(expressionString, prefix, suffix)
				}
			}
		}
	}
}