package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import java.util.concurrent.ConcurrentHashMap

internal class CwtOptionValueConfigResolverImpl : CwtOptionValueConfig.Resolver {
    private val cache = ConcurrentHashMap<String, CwtOptionValueConfig>()

    override fun resolve(
        value: String,
        valueType: CwtType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtOptionValueConfig = doResolve(value, valueType, optionConfigs)

    private fun doResolve(
        value: String,
        valueType: CwtType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtOptionValueConfig {
        // use cache if possible to optimize memory
        if (optionConfigs.isNullOrEmpty()) {
            val cacheKey = "${valueType.ordinal}#${value}"
            return cache.getOrPut(cacheKey) {
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
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId: Byte = valueType.optimizeValue() // use enum id as field to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
}
