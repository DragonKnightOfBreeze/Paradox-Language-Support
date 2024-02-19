package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*

/**
 * CWT键表达式。
 */
class CwtKeyExpression private constructor(
    expressionString: String,
    override val type: CwtDataType,
    override val value: String? = null,
    override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtDataExpression {
    operator fun component1() = type
    
    operator fun component2() = value
    
    companion object Resolver {
        val EmptyExpression = CwtKeyExpression("", CwtDataTypes.Constant, "")
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolve(it) }
        
        fun resolve(expressionString: String): CwtKeyExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtKeyExpression {
            if(expressionString.isEmpty()) return EmptyExpression
            return CwtDataExpressionResolver.resolve(expressionString)
                ?.let { CwtKeyExpression(it.expressionString, it.type, it.value, it.extraValue) }
                ?: CwtKeyExpression(expressionString, CwtDataTypes.Other)
        }
    }
}