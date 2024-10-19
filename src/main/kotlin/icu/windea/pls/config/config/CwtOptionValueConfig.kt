package icu.windea.pls.config.config

import icu.windea.pls.core.*
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

val CwtOptionValueConfig.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtOptionValueConfig.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtOptionValueConfig.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtOptionValueConfig.stringValue: String? get() = if (valueType == CwtType.String) value else null

//Implementations (cached & interned)

private val cache = ConcurrentHashMap<String, CwtOptionValueConfig>()

private fun doResolve(value: String, valueType: CwtType, optionConfigs: List<CwtOptionMemberConfig<*>>?): CwtOptionValueConfig {
    //use cache if possible to optimize memory
    if (optionConfigs.isNullOrEmpty()) {
        val cacheKey = "${valueType.ordinal}#${value}"
        return cache.getOrPut(cacheKey) {
            CwtOptionValueConfigImpl(value, valueType, optionConfigs)
        }
    }
    return CwtOptionValueConfigImpl(value, valueType, optionConfigs)
}

private class CwtOptionValueConfigImpl(
    override val value: String,
    valueType: CwtType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?
) : CwtOptionValueConfig {
    private val valueTypeId: Byte = valueType.optimizeValue() //use enum id as field to optimize memory 
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()
}
