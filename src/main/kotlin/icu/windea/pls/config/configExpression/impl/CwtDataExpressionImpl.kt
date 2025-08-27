package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver
import java.util.concurrent.TimeUnit

internal class CwtDataExpressionResolverImpl : CwtDataExpression.Resolver {
    // 三类缓存分别对应 key / value / template 的解析结果
    // - maximumSize: 限制缓存容量，防止内存无限增长
    // - expireAfterAccess: 非热点条目在一段时间未被访问后回收
    private val cacheForKey = CacheBuilder.newBuilder()
        .maximumSize(4096)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .buildCache<String, CwtDataExpression> { doResolve(it, true) }
    private val cacheForValue = CacheBuilder.newBuilder()
        .maximumSize(4096)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .buildCache<String, CwtDataExpression> { doResolve(it, false) }
    private val cacheForTemplate = CacheBuilder.newBuilder()
        .maximumSize(4096)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .buildCache<String, CwtDataExpression> { doResolveTemplate(it) }

    private val emptyKeyExpression = CwtDataExpressionImpl("", true, CwtDataTypes.Constant).apply { value = "" }
    private val emptyValueExpression = CwtDataExpressionImpl("", false, CwtDataTypes.Constant).apply { value = "" }
    private val blockExpression = CwtDataExpressionImpl("{...}", true, CwtDataTypes.Block)

    override fun resolveEmpty(isKey: Boolean): CwtDataExpression = if (isKey) emptyKeyExpression else emptyValueExpression

    override fun resolveBlock(): CwtDataExpression = blockExpression

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        if (expressionString.isEmpty()) return if (isKey) emptyKeyExpression else emptyValueExpression
        val cache = if (isKey) cacheForKey else cacheForValue
        return cache.get(expressionString)
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression {
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
