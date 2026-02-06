@file:Optimized

package icu.windea.pls.config.config

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.util.elementType
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType
import java.util.*

/**
 * 选项规则。
 *
 * 对应 CWT 规则文件中的一个选项（`## k = v` 或 `## k = {...}`）。需要位于附加到成员上的选项注释中。
 *
 * 用于提供额外的选项数据，自身也可以嵌套下级选项和选项值，以提供更复杂的数据表述。
 *
 * @property key 选项键。
 * @property value 选项值（去除首尾的双引号）。
 * @property valueType 选项值类型，用于后续解析与校验。
 * @property separatorType 分隔符类型。用于为作为条件的选项数据取正或取反。
 *
 * @see CwtOptionComment
 * @see CwtOption
 * @see CwtOptionDataHolder
 */
interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType
    override val value: String
    override val valueType: CwtType

    interface Resolver {
        fun resolve(element: CwtOption): CwtOptionConfig?

        fun create(
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig
    }

    companion object : Resolver by CwtOptionConfigResolverImpl()
}

// region Implementations

private class CwtOptionConfigResolverImpl : CwtOptionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()
    private val cache = CacheBuilder().build<String, CwtOptionConfig>()

    override fun resolve(element: CwtOption): CwtOptionConfig? {
        // - use `EmptyPointer` since visit PSI is not needed
        // - 2.1.1 reduce PSI iterations to optimize performance

        var keyElement: CwtOptionKey? = null
        var valueElement: CwtValue? = null
        var separatorType = CwtSeparatorType.EQUAL
        element.forEachChild { e ->
            when {
                e is CwtOptionKey -> keyElement = e
                e is CwtValue -> valueElement = e
                e.elementType == CwtElementTypes.NOT_EQUAL_SIGN -> separatorType = CwtSeparatorType.NOT_EQUAL
            }
        }

        if (keyElement == null) {
            logger.warn("Missing option key, skipped.".withLocationPrefix(element))
            return null
        }
        if (valueElement == null) {
            logger.warn("Missing option value, skipped.".withLocationPrefix(element))
            return null
        }

        val key = keyElement.value
        val value = valueElement.value
        val valueType = valueElement.type
        val optionConfigs = CwtConfigResolverManager.getOptionConfigsInOption(valueElement)
        return CwtOptionConfig.create(key, value, valueType, separatorType, optionConfigs)
    }

    override fun create(
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?,
    ): CwtOptionConfig {
        val noOptionConfigs = optionConfigs.isNullOrEmpty()
        if (noOptionConfigs) {
            // use (strong) cache if not nested to optimize memory
            val cacheKey = "${valueType.ordinal}#${separatorType.ordinal}#${key}#${value}"
            return cache.get(cacheKey) {
                CwtOptionConfigImpl(key, value, valueType, separatorType)
            }
        }
        return CwtOptionConfigImplNested(key, separatorType, optionConfigs)
    }
}

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private sealed class CwtOptionConfigBase : CwtOptionConfig {
    override fun equals(other: Any?) = this === other || other is CwtOptionConfig
        && key == other.key && value == other.value
        && separatorType == other.separatorType && optionConfigs == other.optionConfigs

    override fun hashCode() = Objects.hash(key, value, separatorType, optionConfigs)
    override fun toString() = "(option) $key $separatorType $value"
}

// 12 + 1 * 1 + 1 * 4 = 17 -> 24
private sealed class CwtOptionConfigImplBase(
    key: String,
    separatorType: CwtSeparatorType,
) : CwtOptionConfigBase() {
    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimized to optimize memory

    override val key: String = key.optimized() // optimized to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())
}

// 12 + 2 * 1 + 2 * 4 = 22 -> 24
private class CwtOptionConfigImpl(
    key: String,
    value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
) : CwtOptionConfigImplBase(key, separatorType) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String = value.optimized() // optimized to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val optionConfigs: List<CwtOptionMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 1 * 1 + 2 * 4 = 21 -> 24
private class CwtOptionConfigImplNested(
    key: String,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfigImplBase(key, separatorType) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block
}

// endregion
