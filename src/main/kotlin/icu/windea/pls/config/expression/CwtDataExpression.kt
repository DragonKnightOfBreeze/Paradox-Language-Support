package icu.windea.pls.config.expression

import com.google.common.cache.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.dataExpression.*

/**
 * CWT数据表达式。
 * @property type 表达式类型，即CWT规则中的dataType。
 */
interface CwtDataExpression : CwtExpression, UserDataHolder {
    val isKey: Boolean
    val type: CwtDataType

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

        fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression {
            return doCreate(expressionString, isKey, type)
        }
    }

    object Keys : KeyRegistry()
}

var CwtDataExpression.value: String? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.intRange: TypedTuple2<Int?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: TypedTuple2<Float?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.ignoreCase: Boolean? by createKey(CwtDataExpression.Keys)

//Implementations (cached & not interned)

private val cacheForKey = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, true) }
private val cacheForValue = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, false) }
private val cacheForTemplate = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolveTemplate(it) }

private fun doResolveEmpty(isKey: Boolean) = CwtDataExpressionImpl("", isKey, CwtDataTypes.Constant).apply { value = "" }

private fun doResolveBlock() = CwtDataExpressionImpl("{...}", true, CwtDataTypes.Block)

private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
    return CwtDataExpressionResolver.resolve(expressionString, isKey)
        ?: CwtDataExpressionImpl(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
}

private fun doResolveTemplate(expressionString: String): CwtDataExpression {
    return CwtDataExpressionResolver.resolveTemplate(expressionString)
        ?: CwtDataExpressionImpl(expressionString, false, CwtDataTypes.Constant).apply { value = expressionString }
}

private fun doCreate(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpressionImpl {
    return CwtDataExpressionImpl(expressionString, isKey, type)
}

private class CwtDataExpressionImpl(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType
) : UserDataHolderBase(), CwtDataExpression {
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
