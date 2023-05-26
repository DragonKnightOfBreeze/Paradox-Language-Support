package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

interface CwtValueAware {
    val value: String
    val valueType: CwtType
    
    val booleanValue: Boolean? get() = if(valueType == CwtType.Boolean) value.toBooleanYesNo() else null
    val intValue: Int? get() = if(valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
    val floatValue: Float? get() = if(valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
    val stringValue: String? get() = if(valueType == CwtType.String) value else null
}


interface CwtPropertyAware : CwtValueAware {
    val key: String
    val separatorType: CwtSeparatorType
}


interface CwtOptionsAware {
    val options: List<CwtOptionConfig>?
    val optionValues: List<CwtOptionValueConfig>?
}

fun CwtOptionsAware.findOption(key: String): CwtOptionConfig? = options?.find { it.key == key }

fun CwtOptionsAware.findOptions(key: String): List<CwtOptionConfig> = options?.filter { it.key == key }.orEmpty()

fun CwtOptionsAware.findOptionValue(value: String): CwtOptionValueConfig? = optionValues?.find { it.value == value }


interface CwtDocumentationAware {
    val documentation: String?
}