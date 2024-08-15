package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

sealed interface CwtMemberConfig<out T : CwtMemberElement> : UserDataHolder, CwtConfig<T> {
    val value: String
    val valueTypeId: @EnumId(CwtType::class) Byte //use enum id to optimize memory
    val valueType: CwtType get() = CwtType.resolve(valueTypeId)
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?
    val documentation: String?
    
    var parentConfig: CwtMemberConfig<*>?
    var inlineableConfig: CwtInlineableConfig<@UnsafeVariance T, CwtMemberConfig<@UnsafeVariance T>>?
    
    val valueExpression: CwtDataExpression
    override val expression: CwtDataExpression
    
    override fun resolved(): CwtMemberConfig<T> = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>() ?: this
    
    override fun resolvedOrNull(): CwtMemberConfig<T>? = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>()
    
    override fun toString(): String
    
    object Keys : KeyRegistry("CwtMemberConfig")
}

val CwtMemberConfig<*>.booleanValue: Boolean? get() = if(valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtMemberConfig<*>.intValue: Int? get() = if(valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtMemberConfig<*>.floatValue: Float? get() = if(valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtMemberConfig<*>.stringValue: String? get() = if(valueType == CwtType.String) value else null

val CwtMemberConfig<*>.values: List<CwtValueConfig>? get() = configs?.filterIsInstance<CwtValueConfig>()
val CwtMemberConfig<*>.properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstance<CwtPropertyConfig>()

val CwtMemberConfig<*>.options: List<CwtOptionConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionConfig>()
val CwtMemberConfig<*>.optionValues: List<CwtOptionValueConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionValueConfig>()

fun CwtMemberConfig<*>.getOptionValue(): String? {
    return stringValue?.intern()
}

fun CwtMemberConfig<*>.getOptionValues(): Set<String>? {
    return optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue?.intern() }
}

fun CwtMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
    return getOptionValue()?.toSingletonSet() ?: getOptionValues()
}

fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtMemberConfig<*>.findOption(predicate: (CwtOptionConfig) -> Boolean): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && predicate(it) }?.cast()
}

fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig>? {
    return optionConfigs?.filter { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtMemberConfig<*>.findOptions(predicate: (CwtOptionConfig) -> Boolean): List<CwtOptionConfig> {
    return optionConfigs?.filter { it is CwtOptionConfig && predicate(it) }.orEmpty().cast()
}

fun CwtMemberConfig<*>.findOptionValue(value: String): CwtOptionValueConfig? {
    return optionConfigs?.find { it is CwtOptionValueConfig && it.value == value }?.cast()
}

fun CwtMemberConfig<*>.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtMemberConfig<*> {
    return when(this) {
        is CwtPropertyConfig -> this.delegated(configs, parentConfig)
        is CwtValueConfig -> this.delegated(configs, parentConfig)
    }
}
