package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*

/**
 * CWT键表达式。
 */
interface CwtKeyExpression : CwtDataExpression {
    operator fun component1() = type
    operator fun component2() = value
    
    companion object Resolver {
        val EmptyExpression: CwtKeyExpression = doResolveEmpty()
        
        fun resolve(expressionString: String): CwtKeyExpression = cache.get(expressionString)
    }
}

//Implementations (cached & interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtKeyExpressionImpl("", CwtDataTypes.Constant, "")

private fun doResolve(expressionString: String): CwtKeyExpression {
    if(expressionString.isEmpty()) return doResolveEmpty()
    return CwtDataExpressionResolver.resolve(expressionString)
        ?.let { CwtKeyExpressionImpl(it.expressionString, it.type, it.value, it.extraValue) }
        ?: CwtKeyExpressionImpl(expressionString, CwtDataTypes.Other)
}

private class CwtKeyExpressionImpl : CwtKeyExpression {
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
    
    override fun equals(other: Any?) = this === other || other is CwtKeyExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}