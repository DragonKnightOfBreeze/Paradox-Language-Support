package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

interface CwtValueAware {
    val value: String
    val valueTypeId: @EnumId(CwtType::class) Byte //use enum id to optimize memory 
    
    val valueType: CwtType get() = CwtType.resolve(valueTypeId)
    
    val booleanValue: Boolean? get() = if(valueType == CwtType.Boolean) value.toBooleanYesNo() else null
    val intValue: Int? get() = if(valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
    val floatValue: Float? get() = if(valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
    val stringValue: String? get() = if(valueType == CwtType.String) value else null
}

interface CwtPropertyAware : CwtValueAware {
    val key: String
    val separatorTypeId: @EnumId(CwtSeparatorType::class) Byte //use enum id to optimize memory 
    
    val separatorType: CwtSeparatorType get() = CwtSeparatorType.resolve(separatorTypeId)
}

interface CwtOptionsAware {
    val options: List<CwtOptionMemberConfig<*>>?
}

@Suppress("NOTHING_TO_INLINE")
inline fun CwtOptionsAware.findOption(key: String): CwtOptionConfig? = options?.find { it is CwtOptionConfig && it.key == key }?.cast()

inline fun CwtOptionsAware.findOption(predicate: (CwtOptionConfig) -> Boolean): CwtOptionConfig? = options?.find { it is CwtOptionConfig && predicate(it) }?.cast()

@Suppress("NOTHING_TO_INLINE")
inline fun CwtOptionsAware.findOptions(key: String): List<CwtOptionConfig>? = options?.filter { it is CwtOptionConfig && it.key == key }?.cast()

inline fun CwtOptionsAware.findOptions(predicate: (CwtOptionConfig) -> Boolean): List<CwtOptionConfig> = options?.filter { it is CwtOptionConfig && predicate(it) }.orEmpty().cast()

@Suppress("NOTHING_TO_INLINE")
inline fun CwtOptionsAware.findOptions(): List<CwtOptionConfig>? = options?.filterIsInstance<CwtOptionConfig>()

@Suppress("NOTHING_TO_INLINE")
inline fun CwtOptionsAware.findOptionValue(value: String): CwtOptionValueConfig? = options?.find { it is CwtOptionValueConfig && it.value == value }?.cast()

@Suppress("NOTHING_TO_INLINE")
inline fun CwtOptionsAware.findOptionValues(): List<CwtOptionValueConfig>? = options?.filterIsInstance<CwtOptionValueConfig>()

interface CwtDocumentationAware {
    val documentation: String?
}