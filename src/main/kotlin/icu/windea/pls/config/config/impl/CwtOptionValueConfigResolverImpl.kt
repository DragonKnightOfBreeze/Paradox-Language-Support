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

internal class CwtOptionValueConfigResolverImpl : CwtOptionValueConfig.Resolver, CwtConfigResolverMixin {
    private val cache = CacheBuilder().build<String, CwtOptionValueConfig>()

    override fun resolve(element: CwtValue): CwtOptionValueConfig {
        val value = element.value
        val valueType = element.type
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigsInOption(element)
        return create(value, valueType, optionConfigs)
    }

    override fun create(value: String, valueType: CwtType, optionConfigs: List<CwtOptionMemberConfig<*>>?): CwtOptionValueConfig {
        // use cache if possible to optimize memory
        if (optionConfigs.isNullOrEmpty()) {
            val cacheKey = "${valueType.ordinal}#${value}"
            return cache.get(cacheKey) {
                CwtOptionValueConfigImpl(value, valueType, optionConfigs)
            }
        }
        return CwtOptionValueConfigImpl(value, valueType, optionConfigs)
    }
}

// 12 + 2 * 4 + 1 * 1 = 21 -> 24
private class CwtOptionValueConfigImpl(
    value: String,
    valueType: CwtType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?
) : CwtOptionValueConfig {
    override val value = value.optimized() // optimized to optimize memory

    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimize memory
    override val valueType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())

    override fun toString() = "(option value) $value"
}
