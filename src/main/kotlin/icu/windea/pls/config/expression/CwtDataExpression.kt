package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.dataExpression.*

/**
 * @property type 表达式类型，即CWT规则中的dataType。
 * @see CwtDataExpressionResolver
 */
interface CwtDataExpression : CwtExpression {
    val type: CwtDataType
    val value: String?
    val extraValue: Any?
    val isKey: Boolean

    @Suppress("UNCHECKED_CAST")
    fun <T> extraValue() = extraValue?.let { it as T }

    companion object Resolver {
        val BlockExpression: CwtDataExpression = doResolveBlock()

        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression = getCache(isKey).get(expressionString)

        fun create(expressionString: String, isKey: Boolean, type: CwtDataType, value: String? = null, extraValue: Any? = null): CwtDataExpression {
            return CwtDataExpressionImpl(expressionString, isKey, type, value, extraValue)
        }
    }
}

//Implementations (cached & interned)

private val cacheForKey = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, true) }
private val cacheForValue = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, false) }

private fun getCache(isKey: Boolean) = if (isKey) cacheForKey else cacheForValue
private fun doResolveEmpty(isKey: Boolean) = CwtDataExpressionImpl("", isKey, CwtDataTypes.Constant, "")
private fun doResolveBlock() = CwtDataExpressionImpl("{...}", true, CwtDataTypes.Block, "{...}")

private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
    if (expressionString.isEmpty()) return doResolveEmpty(isKey)
    return CwtDataExpressionResolver.resolve(expressionString, isKey)
        ?: CwtDataExpressionImpl(expressionString, isKey, CwtDataTypes.Other)
}

private class CwtDataExpressionImpl : CwtDataExpression {
    override val expressionString: String
    override val isKey: Boolean
    override val type: CwtDataType
    override val value: String?
    override val extraValue: Any?

    constructor(expressionString: String, isKey: Boolean, type: CwtDataType, value: String? = null, extraValue: Any? = null) {
        this.expressionString = expressionString.intern()
        this.type = type
        this.value = value
        this.extraValue = extraValue
        this.isKey = isKey
    }

    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}
