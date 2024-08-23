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
            options: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig = doResolve(key, value, valueType, separatorType, options)
    }
}

//Implementations (cached & interned)

private val cache = ConcurrentHashMap<String, CwtOptionConfig>()

private fun doResolve(key: String, value: String, valueType: CwtType, separatorType: CwtSeparatorType, options: List<CwtOptionMemberConfig<*>>?): CwtOptionConfig {
    //use cache if possible to optimize memory
    val valueTypeId = valueType.optimizeValue()
    val separatorTypeId = separatorType.optimizeValue()
    if(options.isNullOrEmpty()) {
        val cacheKey = "$valueTypeId#$separatorTypeId#${key}#${value}"
        return cache.getOrPut(cacheKey) {
            CwtOptionConfigImpl(key, value, valueTypeId, separatorTypeId, options)
        }
    }
    return CwtOptionConfigImpl(key, value, valueTypeId, separatorTypeId, options)
}

private class CwtOptionConfigImpl(
    override val key: String,
    override val value: String,
    private val valueTypeId: Byte, //use enum id as field to optimize memory 
    private val separatorTypeId: Byte, //use enum id as field to optimize memory 
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfig {
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimizeValue()
}
