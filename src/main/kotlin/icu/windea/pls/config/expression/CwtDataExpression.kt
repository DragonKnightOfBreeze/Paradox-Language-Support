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
        val EmptyKeyExpression: CwtDataExpression = doResolveEmpty(true)
        val EmptyValueExpression: CwtDataExpression = doResolveEmpty(false)
        val BlockExpression: CwtDataExpression = doResolveBlock()

        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
            if (expressionString.isEmpty()) return if (isKey) EmptyKeyExpression else EmptyValueExpression
            val cache = if (isKey) cacheForKey else cacheForValue
            return cache.get(expressionString)
        }

        fun resolveTemplate(expressionString: String): CwtDataExpression {
            if (expressionString.isEmpty()) return EmptyValueExpression
            return cacheForTemplate.get(expressionString)
        }

        fun create(expressionString: String, isKey: Boolean, type: CwtDataType, value: String? = null, extraValue: Any? = null): CwtDataExpression {
            return doCreate(expressionString, isKey, type, value, extraValue)
        }
    }
}

//Implementations (cached & not interned)

private val cacheForKey = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, true) }
private val cacheForValue = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, false) }
private val cacheForTemplate = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolveTemplate(it) }

private fun doResolveEmpty(isKey: Boolean) = CwtDataExpressionImpl("", isKey, CwtDataTypes.Constant, "")

private fun doResolveBlock() = CwtDataExpressionImpl("{...}", true, CwtDataTypes.Block, "{...}")

private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
    return CwtDataExpressionResolver.resolve(expressionString, isKey) ?: doCreate(expressionString, isKey, CwtDataTypes.Constant)
}

private fun doResolveTemplate(expressionString: String): CwtDataExpression {
    return CwtDataExpressionResolver.resolveTemplate(expressionString) ?: doCreate(expressionString, false, CwtDataTypes.Constant)
}

private fun doCreate(expressionString: String, isKey: Boolean, type: CwtDataType, value: String? = null, extraValue: Any? = null): CwtDataExpressionImpl {
    return CwtDataExpressionImpl(expressionString, isKey, type, value, extraValue)
}

private class CwtDataExpressionImpl(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType,
    override val value: String? = null,
    override val extraValue: Any? = null
) : CwtDataExpression {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtDataExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int {
        return expressionString.hashCode()
    }

    override fun toString(): String {
        return expressionString
    }
}
