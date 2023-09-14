package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.util.*

open class CwtSimpleTemplateExpression(
    expressionString: String,
    val prefixSnippet: String,
    val suffixSnippet: String
) : AbstractExpression(expressionString), CwtExpression {
    companion object Resolver {
        val EmptyExpression = CwtSimpleTemplateExpression("", "", "")
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtSimpleTemplateExpression> { doResolve(it) }
        
        // $_desc
        
        fun resolve(expressionString: String): CwtSimpleTemplateExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtSimpleTemplateExpression {
            return when {
                expressionString.isEmpty() -> EmptyExpression
                else -> {
                    val i = expressionString.indexOf('$')
                    if(i == -1) CwtSimpleTemplateExpression(expressionString, expressionString, "")
                    else {
                        val prefix = expressionString.substring(0, i).intern()
                        val suffix = expressionString.substring(i + 1).intern()
                        CwtSimpleTemplateExpression(expressionString, prefix, suffix)
                    }
                }
            }
        }
    }
}