package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.core.cast
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.model.CwtType

/**
 * CWT 选项成员规则。
 *
 * 用于修饰/限定某个成员规则的语义，通常以 `key = value` 或独立值的形式出现在成员规则的选项列表中。
 *
 * @param T 对应的 PSI 类型（实现上为 [icu.windea.pls.cwt.psi.CwtOption]）。
 * @property value 选项的原始文本值。
 * @property valueType 值类型 [CwtType]。
 * @property optionConfigs 嵌套的选项（少量规则会在选项下继续声明选项）。
 */
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
    return getOptionValue()?.singleton?.set() ?: getOptionValues()
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
