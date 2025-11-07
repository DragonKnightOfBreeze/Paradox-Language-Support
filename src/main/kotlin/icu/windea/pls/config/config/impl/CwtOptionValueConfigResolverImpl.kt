package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.forCwtType
import java.util.*

internal class CwtOptionValueConfigResolverImpl : CwtOptionValueConfig.Resolver, CwtConfigResolverMixin {
    private val cache = CacheBuilder().build<String, CwtOptionValueConfig>()

    override fun resolve(element: CwtValue): CwtOptionValueConfig {
        val value = element.value
        val valueType = element.type
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigsInOption(element)
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
        return CwtOptionValueConfigImplNested(value, optionConfigs)
    }
}

private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private abstract class CwtOptionValueConfigBase : CwtOptionValueConfig {
    override fun equals(other: Any?) = this === other || other is CwtOptionValueConfig
        && value == other.value && optionConfigs == other.optionConfigs

    override fun hashCode() = Objects.hash(value, optionConfigs)
    override fun toString() = "(option value) $value"
}

private abstract class CwtOptionValueConfigImplBase(
    value: String,
) : CwtOptionValueConfigBase() {
    override val value: String = value.optimized() // optimized to optimize memory
}

private class CwtOptionValueConfigImpl(
    value: String,
    valueType: CwtType,
) : CwtOptionValueConfigImplBase(value) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val optionConfigs: List<CwtOptionMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

private class CwtOptionValueConfigImplNested(
    value: String,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionValueConfigImplBase(value) {
    override val valueType: CwtType get() = CwtType.Block
}
