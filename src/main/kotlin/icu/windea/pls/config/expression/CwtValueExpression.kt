package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*

/**
 * CWT值表达式。
 * @property type 类型
 * @property value 值
 */
class CwtValueExpression private constructor(
    expressionString: String,
    override val type: CwtDataType,
    override val value: String? = null,
    override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtDataExpression {
    operator fun component1() = type
    
    operator fun component2() = value
    
    companion object Resolver {
        val EmptyExpression = CwtValueExpression("", CwtDataTypes.Constant, "")
        val BlockExpression = CwtValueExpression("{...}", CwtDataTypes.Block, "{...}")
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtValueExpression> { doResolve(it) }
        
        fun resolve(expressionString: String): CwtValueExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtValueExpression {
            if(expressionString.isEmpty()) return EmptyExpression
            return CwtDataExpressionResolver.resolve(expressionString, false)
                ?.run { CwtValueExpression(expressionString, type, value, extraValue) }
                ?: CwtValueExpression(expressionString, CwtDataTypes.Other)
        }
    }
}
