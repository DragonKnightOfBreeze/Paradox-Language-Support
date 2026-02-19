package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver
import icu.windea.pls.model.constants.PlsStrings

/**
 * 数据表达式。
 *
 * 用于描述脚本文件中的表达式（键或值）的匹配模式，基于数据类型以及数种元数据。
 *
 * 说明：
 * - 对应的数据类型可通过 [type] 获取。
 * - 主要的元数据可通过 [value] 获取。
 * - 额外的元数据会存储到 [UserDataHolder] 中，可通过扩展属性获取。
 *
 * 适用对象：定义成员对应的规则的键或值。
 *
 * CWTools 兼容性：兼容，但存在较多扩展与改进。
 *
 * @property isKey 是否来源于作为键的表达式。
 * @property type 解析得到的数据类型。
 * @property value 主要的元数据。可用于存储定义类型、枚举名等信息。
 *
 * @see CwtDataType
 * @see CwtDataExpressionResolver
 */
interface CwtDataExpression : CwtConfigExpression, UserDataHolder {
    val isKey: Boolean
    val type: CwtDataType
    var value: String?

    interface Resolver {
        fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression
        fun resolveEmpty(isKey: Boolean): CwtDataExpression
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression
        fun resolveKey(expressionString: String): CwtDataExpression
        fun resolveValue(expressionString: String): CwtDataExpression
        fun resolveTemplate(expressionString: String): CwtDataExpression
        fun resolveBlock(): CwtDataExpression
    }

    object Keys : KeyRegistry()

    companion object : Resolver by CwtDataExpressionResolverImpl()
}

// region Implementations

private class CwtDataExpressionResolverImpl : CwtDataExpression.Resolver {
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

    override fun resolveEmpty(isKey: Boolean): CwtDataExpression {
        return if (isKey) emptyKeyExpression else emptyValueExpression
    }

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        if (expressionString.isEmpty()) return if (isKey) emptyKeyExpression else emptyValueExpression
        val cache = if (isKey) cacheForKey else cacheForValue
        return cache.get(expressionString)
    }

    override fun resolveKey(expressionString: String): CwtDataExpression {
        return resolve(expressionString, true)
    }

    override fun resolveValue(expressionString: String): CwtDataExpression {
        return resolve(expressionString, false)
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression {
        if (expressionString.isEmpty()) return emptyValueExpression
        return cacheForTemplate.get(expressionString)
    }

    override fun resolveBlock(): CwtDataExpression {
        return blockExpression
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

// endregion
