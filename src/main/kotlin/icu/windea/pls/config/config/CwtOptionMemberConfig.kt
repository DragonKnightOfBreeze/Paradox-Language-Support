package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.core.cast
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.model.CwtType

sealed interface CwtOptionMemberConfig<out T : PsiElement> : CwtDetachedConfig {
    val value: String
    val valueType: CwtType
    val optionConfigs: List<CwtOptionMemberConfig<*>>?
}

val CwtOptionMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtOptionMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtOptionMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtOptionMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

val CwtOptionMemberConfig<*>.options: List<CwtOptionConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionConfig>()
val CwtOptionMemberConfig<*>.optionValues: List<CwtOptionValueConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionValueConfig>()

fun CwtOptionMemberConfig<*>.getOptionValue(): String? {
    return stringValue
}

fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
    return optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue?.intern() }
}

fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
    return getOptionValue()?.let { it.singleton.set() } ?: getOptionValues()
}

fun CwtOptionMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtOptionMemberConfig<*>.findOption(predicate: (CwtOptionConfig) -> Boolean): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && predicate(it) }?.cast()
}

fun CwtOptionMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig>? {
    return optionConfigs?.filter { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtOptionMemberConfig<*>.findOptions(predicate: (CwtOptionConfig) -> Boolean): List<CwtOptionConfig> {
    return optionConfigs?.filter { it is CwtOptionConfig && predicate(it) }.orEmpty().cast()
}

fun CwtOptionMemberConfig<*>.findOptionValue(value: String): CwtOptionValueConfig? {
    return optionConfigs?.find { it is CwtOptionValueConfig && it.value == value }?.cast()
}
