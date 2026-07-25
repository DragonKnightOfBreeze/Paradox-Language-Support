@file:Optimized

package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * 数据表达式。
 *
 * 用于描述脚本文件中的表达式（键或值）的匹配模式，基于数据类型以及可选的元数据。
 *
 * 说明：
 * - 可通过 [type] 获取对应的数据类型。
 * - 可通过 [metadata] 获取对应的元数据。
 *
 * 适用对象：
 * - 定义成员对应的规则的键或值。
 *
 * 示例：
 *
 * ```cwt
 * int                         # 整数
 * float[0.0..1.0]             # 带范围约束的浮点数
 * enum[ship_size]        # 枚举引用
 * scope[country]              # 作用域引用
 * <ship_size>                 # 定义引用
 * value[event_target]         # 动态值引用
 * pre_<opinion_modifier>_suf  # 模板表达式（含定义引用片段）
 * ```
 *
 * > CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。
 *
 * @property isKey 是否来源于作为键的表达式。
 * @property type 数据类型。
 * @property metadata 可选的元数据。
 *
 * @see CwtDataType
 * @see CwtDataExpressionMetadata
 * @see CwtDataExpressionSupport
 * @see ParadoxExpression
 */
interface CwtDataExpression : CwtConfigExpression {
    val isKey: Boolean
    val type: CwtDataType
    val metadata: CwtDataExpressionMetadata

    override fun equals(other: Any?): Boolean // NOTE 3.0.1 only based on `expressionString`
    override fun hashCode(): Int // NOTE 3.0.1 only based on `expressionString`
    override fun toString(): String

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(expressionString: String, isKey: Boolean, type: CwtDataType, metadataBuilder: CwtDataExpressionMetadataBuilder? = null): CwtDataExpression {
            return CwtDataExpressionResolver.create(expressionString, isKey, type, metadataBuilder)
        }

        @JvmStatic
        fun resolveEmpty(isKey: Boolean): CwtDataExpression {
            return CwtDataExpressionResolver.resolveEmpty(isKey)
        }

        @JvmStatic
        fun resolveBlock(): CwtDataExpression {
            return CwtDataExpressionResolver.resolveBlock()
        }

        @JvmStatic
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
            return CwtDataExpressionResolver.resolve(expressionString, isKey)
        }

        @JvmStatic
        fun resolveKey(expressionString: String): CwtDataExpression {
            return CwtDataExpressionResolver.resolveKey(expressionString)
        }

        @JvmStatic
        fun resolveValue(expressionString: String): CwtDataExpression {
            return CwtDataExpressionResolver.resolveValue(expressionString)
        }

        @JvmStatic
        fun resolveTemplate(expressionString: String): CwtDataExpression {
            return CwtDataExpressionResolver.resolveTemplate(expressionString)
        }
    }
}

// region Implementations

private object CwtDataExpressionResolver {
    private val cacheForKey = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolve(it, true) }
    private val cacheForValue = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolve(it, false) }
    private val cacheForTemplate = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolveTemplate(it) }

    private val emptyKeyExpression = CwtDataExpressionImplWithMetadata("", true, CwtDataTypes.Constant).apply { value = "" }
    private val emptyValueExpression = CwtDataExpressionImplWithMetadata("", false, CwtDataTypes.Constant).apply { value = "" }
    private val blockExpression = create(ChronicleStrings.blockFolder, false, CwtDataTypes.Block)

    fun create(expressionString: String, isKey: Boolean, type: CwtDataType, metadataBuilder: CwtDataExpressionMetadataBuilder? = null): CwtDataExpression {
        if (expressionString.isEmpty()) return resolveEmpty(isKey)
        if (metadataBuilder == null) return CwtDataExpressionImplWithoutMetadata(expressionString, isKey, type)
        return CwtDataExpressionImplWithMetadata(expressionString, isKey, type).apply(metadataBuilder)
    }

    fun resolveEmpty(isKey: Boolean): CwtDataExpression {
        return if (isKey) emptyKeyExpression else emptyValueExpression
    }

    fun resolveBlock(): CwtDataExpression {
        return blockExpression
    }

    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        if (expressionString.isEmpty()) return if (isKey) emptyKeyExpression else emptyValueExpression
        val cache = if (isKey) cacheForKey else cacheForValue
        return cache.get(expressionString)
    }

    fun resolveKey(expressionString: String): CwtDataExpression {
        return resolve(expressionString, true)
    }

    fun resolveValue(expressionString: String): CwtDataExpression {
        return resolve(expressionString, false)
    }

    fun resolveTemplate(expressionString: String): CwtDataExpression {
        if (expressionString.isEmpty()) return emptyValueExpression
        return cacheForTemplate.get(expressionString)
    }

    private fun doResolve(expressionString: String, isKey: Boolean): CwtDataExpression {
        return CwtConfigExpressionService.resolve(expressionString, isKey)
            ?: create(expressionString, isKey, CwtDataTypes.Constant) { value = expressionString }
    }

    private fun doResolveTemplate(expressionString: String): CwtDataExpression {
        return CwtConfigExpressionService.resolveTemplate(expressionString)
            ?: create(expressionString, false, CwtDataTypes.Constant) { value = expressionString }
    }
}

private class CwtDataExpressionImplWithoutMetadata(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType,
) : CwtDataExpression {
    override val metadata get() = CwtDataExpressionMetadata.EMPTY

    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

private class CwtDataExpressionImplWithMetadata(
    override val expressionString: String,
    override val isKey: Boolean,
    override val type: CwtDataType,
) : CwtDataExpressionMetadataBase(), CwtDataExpression {
    override val metadata: CwtDataExpressionMetadata get() = this

    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion
