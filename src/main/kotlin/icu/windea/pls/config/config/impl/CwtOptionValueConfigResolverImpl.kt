@file:Optimized

package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.model.forCwtType
import java.util.*

internal class CwtOptionValueConfigResolverImpl : CwtOptionValueConfig.Resolver, CwtConfigResolverMixin {
    private val cache = CacheBuilder().build<String, CwtOptionValueConfig>()

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

    override fun resolve(element: CwtValue): CwtOptionValueConfig {
        val value = element.value
        val valueType = element.type
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigsInOption(element)
        return create(value, valueType, optionConfigs)
    }
}

private const val blockValue = PlsStringConstants.blockFolder
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
