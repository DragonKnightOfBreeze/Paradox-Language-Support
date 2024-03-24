package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.expression.*

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

//Implementations (cached & interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtValueExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtValueExpressionImpl("", CwtDataTypes.Constant, "")
private fun doResolveBlock() = CwtValueExpressionImpl("{...}", CwtDataTypes.Block, "{...}")

private fun doResolve(expressionString: String): CwtValueExpression {
    if(expressionString.isEmpty()) return doResolveEmpty()
    return CwtDataExpressionResolver.resolve(expressionString)
        ?.let { CwtValueExpressionImpl(it.expressionString, it.type, it.value, it.extraValue) }
        ?: CwtValueExpressionImpl(expressionString, CwtDataTypes.Other)
}

private class CwtValueExpressionImpl : CwtValueExpression {
    override val expressionString: String
    override val type: CwtDataType
    override val value: String?
    override val extraValue: Any?
    
    constructor(expressionString: String, type: CwtDataType, value: String? = null, extraValue: Any? = null) {
        this.expressionString = expressionString.intern()
        this.type = type
        this.value = value
        this.extraValue = extraValue
    }
    
    override fun equals(other: Any?) = this === other || other is CwtValueExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}
