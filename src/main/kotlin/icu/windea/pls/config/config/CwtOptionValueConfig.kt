@file:Optimized

package icu.windea.pls.config.config

import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtType
import java.util.*

/**
 * 选项值规则。
 *
 * 对应 CWT 规则文件中的一个没有键的选项值（`## v`）。需要位于附加到成员上的选项注释中。
 *
 * 用于提供额外的选项数据，自身也可以嵌套下级选项和选项值，以提供更复杂的数据表述。
 * 在选项注释中单独使用时，常用来提供布尔标志或较短的语义标签。
 *
 * @see CwtOptionComment
 * @see CwtValue
 * @see CwtOptionDataHolder
 */
interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtValue> {
    interface Resolver {
        fun resolve(element: CwtValue): CwtOptionValueConfig

        fun create(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionValueConfig
    }

    companion object : Resolver by CwtOptionValueConfigResolverImpl()
}

// region Implementations

private class CwtOptionValueConfigResolverImpl : CwtOptionValueConfig.Resolver, CwtConfigResolverScope {
    private val cache = CacheBuilder().build<String, CwtOptionValueConfig>()

    override fun resolve(element: CwtValue): CwtOptionValueConfig {
        val value = element.value
        val valueType = element.type
        val optionConfigs = CwtConfigResolverManager.getOptionConfigsInOption(element)
        return create(value, valueType, optionConfigs)
    }

    override fun create(value: String, valueType: CwtType, optionConfigs: List<CwtOptionMemberConfig<*>>?): CwtOptionValueConfig {
        val noOptionConfigs = optionConfigs.isNullOrEmpty()
        if (noOptionConfigs) {
            // use (strong) cache if not nested to optimize memory
            val cacheKey = "${valueType.ordinal}#${value}"
            return cache.get(cacheKey) {
                CwtOptionValueConfigImpl(value, valueType)
            }
        }
        return CwtOptionValueConfigImplNested(optionConfigs)
    }
}

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private abstract class CwtOptionValueConfigBase : CwtOptionValueConfig {
    override fun equals(other: Any?) = this === other || other is CwtOptionValueConfig
        && value == other.value && optionConfigs == other.optionConfigs

    override fun hashCode() = Objects.hash(value, optionConfigs)
    override fun toString() = "(option value) $value"
}

// 12 = 12 -> 16
private abstract class CwtOptionValueConfigImplBase : CwtOptionValueConfigBase()

// 12 + 1 * 1 + 1 * 4 = 17 -> 24
private class CwtOptionValueConfigImpl(
    value: String,
    valueType: CwtType,
) : CwtOptionValueConfigImplBase() {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String = value.optimized() // optimized to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val optionConfigs: List<CwtOptionMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 1 * 4 = 16 -> 16
private class CwtOptionValueConfigImplNested(
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionValueConfigImplBase() {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block
}

// endregion
