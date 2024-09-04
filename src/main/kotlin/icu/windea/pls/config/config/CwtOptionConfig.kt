package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType
    
    companion object Resolver {
        fun resolve(
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig = doResolve(key, value, valueType, separatorType, optionConfigs)
    }
}

//Implementations (cached & interned)

private val cache = ConcurrentHashMap<String, CwtOptionConfig>()

private fun doResolve(key: String, value: String, valueType: CwtType, separatorType: CwtSeparatorType, optionConfigs: List<CwtOptionMemberConfig<*>>?): CwtOptionConfig {
    //use cache if possible to optimize memory
    if(optionConfigs.isNullOrEmpty()) {
        val cacheKey = "${valueType.ordinal}#${separatorType.ordinal}#${key}#${value}"
        return cache.getOrPut(cacheKey) {
            CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
        }
    }
    return CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
}

private class CwtOptionConfigImpl(
    override val key: String,
    override val value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfig {
    private val valueTypeId: Byte = valueType.optimizeValue() //use enum id as field to optimize memory 
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
    
    private val separatorTypeId: Byte = separatorType.optimizeValue() //use enum id as field to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimizeValue()
}
