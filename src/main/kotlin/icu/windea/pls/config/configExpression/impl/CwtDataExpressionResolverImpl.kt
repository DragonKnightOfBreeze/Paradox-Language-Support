@file:Optimized

package icu.windea.pls.config.configExpression.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtConfigExpressionService
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.model.constants.PlsStrings

internal class CwtDataExpressionResolverImpl : CwtDataExpression.Resolver {
    private val cacheForKey = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtDataExpression> { doResolve(it, true) }
    private val cacheForValue = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtDataExpression> { doResolve(it, false) }
    private val cacheForTemplate = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtDataExpression> { doResolveTemplate(it) }

    private val emptyKeyExpression = CwtDataExpressionImpl("", true, CwtDataTypes.Constant, "")
    private val emptyValueExpression = CwtDataExpressionImpl("", false, CwtDataTypes.Constant, "")
    private val blockExpression = CwtDataExpressionImpl(PlsStrings.blockFolder, false, CwtDataTypes.Block)

    override fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression {
        if (expressionString.isEmpty()) return resolveEmpty(isKey)
        return CwtDataExpressionImpl(expressionString, isKey, type)
    }

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

    private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        return CwtConfigExpressionService.resolve(expressionString, isKey)
            ?: CwtDataExpressionImpl(expressionString, isKey, CwtDataTypes.Constant, expressionString)
    }

    private fun doResolveTemplate(expressionString: String): CwtDataExpression {
        return CwtConfigExpressionService.resolveTemplate(expressionString)
            ?: CwtDataExpressionImpl(expressionString, false, CwtDataTypes.Constant, expressionString)
    }
}

private class CwtDataExpressionImpl(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType,
    override var value: String? = null,
) : UserDataHolderBase(), CwtDataExpression {
    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}
