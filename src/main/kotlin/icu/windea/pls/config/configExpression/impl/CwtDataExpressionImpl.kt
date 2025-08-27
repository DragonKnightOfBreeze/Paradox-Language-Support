package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver

internal class CwtDataExpressionResolverImpl : CwtDataExpression.Resolver {
    // 缓存：按“键/值/模板”分别缓存解析结果，降低解析开销
    private val cacheForKey = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, true) }
    private val cacheForValue = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolve(it, false) }
    private val cacheForTemplate = CacheBuilder.newBuilder().buildCache<String, CwtDataExpression> { doResolveTemplate(it) }

    // 预构建的空/块表达式，频繁复用；空表达式附加 value="" 便于后续使用
    private val emptyKeyExpression = CwtDataExpressionImpl("", true, CwtDataTypes.Constant).apply { value = "" }
    private val emptyValueExpression = CwtDataExpressionImpl("", false, CwtDataTypes.Constant).apply { value = "" }
    private val blockExpression = CwtDataExpressionImpl("{...}", true, CwtDataTypes.Block)

    override fun resolveEmpty(isKey: Boolean): CwtDataExpression = if (isKey) emptyKeyExpression else emptyValueExpression

    override fun resolveBlock(): CwtDataExpression = blockExpression

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        // 空字符串走空表达式分支（避免缓存空键）
        if (expressionString.isEmpty()) return resolveEmpty(isKey)
        val cache = if (isKey) cacheForKey else cacheForValue
        return cache.get(expressionString)
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression {
        // 模板解析：空字符串直接返回空值表达式
        if (expressionString.isEmpty()) return emptyValueExpression
        return cacheForTemplate.get(expressionString)
    }

    override fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression {
        return CwtDataExpressionImpl(expressionString, isKey, type)
    }

    private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        // 委托 EP 解析；若无匹配规则则回退为 Constant，并把原值写入扩展属性 value
        return CwtDataExpressionResolver.resolve(expressionString, isKey)
            ?: CwtDataExpressionImpl(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
    }

    private fun doResolveTemplate(expressionString: String): CwtDataExpression {
        // 模板专用解析；同样支持回退为 Constant
        return CwtDataExpressionResolver.resolveTemplate(expressionString)
            ?: CwtDataExpressionImpl(expressionString, false, CwtDataTypes.Constant).apply { value = expressionString }
    }
}

private class CwtDataExpressionImpl(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType
) : UserDataHolderBase(), CwtDataExpression {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtDataExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
