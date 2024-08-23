package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.CwtOption
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

val CwtOptionValueConfig.booleanValue: Boolean? get() = if(valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtOptionValueConfig.intValue: Int? get() = if(valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtOptionValueConfig.floatValue: Float? get() = if(valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtOptionValueConfig.stringValue: String? get() = if(valueType == CwtType.String) value else null

//Implementations (cached & interned)

private val cache = ConcurrentHashMap<String, CwtOptionValueConfig>()

private fun doResolve(value: String, valueType: CwtType, options: List<CwtOptionMemberConfig<*>>?): CwtOptionValueConfig {
    //use cache if possible to optimize memory
    val valueTypeId = valueType.optimizeValue()
    if(options.isNullOrEmpty()) {
        val cacheKey = "$valueTypeId#${value}"
        return cache.getOrPut(cacheKey) {
            CwtOptionValueConfigImpl(value, valueTypeId, options)
        }
    }
    return CwtOptionValueConfigImpl(value, valueTypeId, options)
}

private class CwtOptionValueConfigImpl(
    override val value: String,
    private val valueTypeId: Byte, //use enum id as field to optimize memory 
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?
) : CwtOptionValueConfig {
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
}
