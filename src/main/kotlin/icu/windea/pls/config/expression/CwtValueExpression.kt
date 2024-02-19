package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*

/**
 * CWT值表达式。
 */
interface CwtValueExpression : CwtDataExpression {
    operator fun component1() = type
    operator fun component2() = value
    
    companion object Resolver {
        val EmptyExpression: CwtValueExpression = doResolveEmpty()
        val BlockExpression: CwtValueExpression = doResolveBlock()
        
        fun resolve(expressionString: String): CwtValueExpression = cache.get(expressionString)
    }
}

//region Resolve Methods

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtValueExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtValueExpressionImpl("", CwtDataTypes.Constant, "")
private fun doResolveBlock() = CwtValueExpressionImpl("{...}", CwtDataTypes.Block, "{...}")

private fun doResolve(expressionString: String): CwtValueExpression {
    if(expressionString.isEmpty()) return doResolveEmpty()
    return CwtDataExpressionResolver.resolve(expressionString)
        ?.let { CwtValueExpressionImpl(it.expressionString, it.type, it.value, it.extraValue) }
        ?: CwtValueExpressionImpl(expressionString, CwtDataTypes.Other)
}

//region Implementations

private class CwtValueExpressionImpl(
    override val expressionString: String,
    override val type: CwtDataType,
    override val value: String? = null,
    override val extraValue: Any? = null
) : CwtValueExpression {
    override fun equals(other: Any?) = this === other || other is CwtValueExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}
