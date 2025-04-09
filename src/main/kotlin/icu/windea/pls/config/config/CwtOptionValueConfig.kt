package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtOption> {
    companion object Resolver {
        fun resolve(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        ): CwtOptionValueConfig = doResolve(value, valueType, optionConfigs)
    }
}

//Implementations (cached if possible & interned)

private val cache = ConcurrentHashMap<String, CwtOptionValueConfig>()

private fun doResolve(
    value: String,
    valueType: CwtType,
    optionConfigs: List<CwtOptionMemberConfig<*>>?
): CwtOptionValueConfig {
    //use cache if possible to optimize memory
    if (optionConfigs.isNullOrEmpty()) {
        val cacheKey = "${valueType.ordinal}#${value}"
        return cache.getOrPut(cacheKey) {
            CwtOptionValueConfigImpl(value, valueType, optionConfigs)
        }
    }
    return CwtOptionValueConfigImpl(value, valueType, optionConfigs)
}

//12 + 2 * 4 + 1 * 1 = 21 -> 24
private class CwtOptionValueConfigImpl(
    value: String,
    valueType: CwtType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?
) : CwtOptionValueConfig {
    override val value = value.intern() //intern to optimize memory

    private val valueTypeId: Byte = valueType.optimizeValue() //use enum id as field to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
}
