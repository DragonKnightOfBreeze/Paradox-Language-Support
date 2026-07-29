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
 * @property type 数据类型。
 * @property role 角色。分为键/值/其他。
 * @property metadata 可选的元数据。
 *
 * @see CwtDataType
 * @see CwtDataExpressionMetadata
 * @see CwtDataExpressionSupport
 * @see ParadoxExpression
 */
interface CwtDataExpression : CwtConfigExpression {
    val type: CwtDataType
    val role: CwtDataExpressionRole
    val metadata: CwtDataExpressionMetadata

    override fun equals(other: Any?): Boolean // NOTE 3.0.1 only based on `expressionString`
    override fun hashCode(): Int // NOTE 3.0.1 only based on `expressionString`
    override fun toString(): String

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(expressionString: String, type: CwtDataType, role: CwtDataExpressionRole = CwtDataExpressionRole.Other, metadataBuilder: CwtDataExpressionMetadataBuilder? = null): CwtDataExpression {
            return CwtDataExpressionResolver.create(expressionString, type, role, metadataBuilder)
        }

        @JvmStatic
        fun resolveEmpty(role: CwtDataExpressionRole = CwtDataExpressionRole.Other): CwtDataExpression {
            return CwtDataExpressionResolver.resolveEmpty(role)
        }

        @JvmStatic
        fun resolveBlock(): CwtDataExpression {
            return CwtDataExpressionResolver.resolveBlock()
        }

        @JvmStatic
        fun resolve(expressionString: String, role: CwtDataExpressionRole = CwtDataExpressionRole.Other): CwtDataExpression {
            return CwtDataExpressionResolver.resolve(expressionString, role)
        }

        @JvmStatic
        fun resolveTemplate(expressionString: String): CwtDataExpression {
            return CwtDataExpressionResolver.resolveTemplate(expressionString)
        }
    }
}

// region Implementations

private object CwtDataExpressionResolver {
    private val cacheForKey = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolve(it, CwtDataExpressionRole.Key) }
    private val cacheForValue = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolve(it, CwtDataExpressionRole.Value) }
    private val cacheForOther = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolve(it, CwtDataExpressionRole.Other) }
    private val cacheForTemplate = CacheBuilder("expireAfterAccess=30m").build<String, CwtDataExpression> { doResolveTemplate(it) }

    private val emptyKeyExpression = CwtDataExpressionImplWithoutMetadata("", CwtDataTypes.Constant, CwtDataExpressionRole.Key)
    private val emptyValueExpression = CwtDataExpressionImplWithoutMetadata("", CwtDataTypes.Constant, CwtDataExpressionRole.Value)
    private val emptyOtherExpression = CwtDataExpressionImplWithoutMetadata("", CwtDataTypes.Constant, CwtDataExpressionRole.Other)
    private val blockExpression = CwtDataExpressionImplWithoutMetadata(ChronicleStrings.blockFolder, CwtDataTypes.Block, CwtDataExpressionRole.Value)

    fun create(expressionString: String, type: CwtDataType, role: CwtDataExpressionRole, metadataBuilder: CwtDataExpressionMetadataBuilder?): CwtDataExpression {
        if (expressionString.isEmpty()) return resolveEmpty(role)
        if (metadataBuilder == null) return CwtDataExpressionImplWithoutMetadata(expressionString, type, role)
        return CwtDataExpressionImplWithMetadata(expressionString, type, role).apply(metadataBuilder)
    }

    fun resolveEmpty(role: CwtDataExpressionRole): CwtDataExpression {
        return when (role) {
            CwtDataExpressionRole.Key -> emptyKeyExpression
            CwtDataExpressionRole.Value -> emptyValueExpression
            CwtDataExpressionRole.Other -> emptyOtherExpression
        }
    }

    fun resolveBlock(): CwtDataExpression {
        return blockExpression
    }

    fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression {
        if (expressionString.isEmpty()) return resolveEmpty(role)
        val cache = when (role) {
            CwtDataExpressionRole.Key -> cacheForKey
            CwtDataExpressionRole.Value -> cacheForValue
            CwtDataExpressionRole.Other -> cacheForOther
        }
        return cache.get(expressionString)
    }

    fun resolveTemplate(expressionString: String): CwtDataExpression {
        if (expressionString.isEmpty()) return emptyOtherExpression
        val cache = cacheForTemplate
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression {
        return CwtConfigExpressionService.resolve(expressionString, role) ?: CwtDataExpressionImplWithoutMetadata(expressionString, CwtDataTypes.Constant, role)
    }

    private fun doResolveTemplate(expressionString: String): CwtDataExpression {
        return CwtConfigExpressionService.resolveTemplate(expressionString) ?: CwtDataExpressionImplWithoutMetadata(expressionString, CwtDataTypes.Constant, CwtDataExpressionRole.Other)
    }
}

private class CwtDataExpressionImplWithoutMetadata(
    override val expressionString: String,
    override val type: CwtDataType,
    override val role: CwtDataExpressionRole,
) : CwtDataExpression {
    override val metadata get() = CwtDataExpressionMetadata.Empty

    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

private class CwtDataExpressionImplWithMetadata(
    override val expressionString: String,
    override val type: CwtDataType,
    override val role: CwtDataExpressionRole,
) : CwtDataExpressionMetadataBase(), CwtDataExpression {
    override val metadata: CwtDataExpressionMetadata get() = this

    override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion

