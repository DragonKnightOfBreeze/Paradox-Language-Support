package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import java.util.concurrent.ConcurrentHashMap

internal class CwtOptionConfigResolverImpl : CwtOptionConfig.Resolver {
    private val cache = ConcurrentHashMap<String, CwtOptionConfig>()

    override fun resolve(
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?,
    ): CwtOptionConfig = doResolve(key, value, valueType, separatorType, optionConfigs)

    private fun doResolve(
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtOptionConfig {
        // use cache if possible to optimize memory
        if (optionConfigs.isNullOrEmpty()) {
            val cacheKey = "${valueType.ordinal}#${separatorType.ordinal}#${key}#${value}"
            return cache.getOrPut(cacheKey) {
                CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
            }
        }
        return CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
    }
}

// 12 + 3 * 4 + 2 * 1 = 26 -> 32
private class CwtOptionConfigImpl(
    key: String,
    value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfig {
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId = valueType.optimizeValue() // use enum id as field to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    private val separatorTypeId = separatorType.optimizeValue() // use enum id as field to optimize memory
    override val separatorType get() = separatorTypeId.deoptimizeValue<CwtSeparatorType>()

    override fun toString() = "(option) $key $separatorType $value"
}
