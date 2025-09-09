package icu.windea.pls.config.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.model.CwtType

sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    val value: String
    val valueType: CwtType
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    var parentConfig: CwtMemberConfig<*>?

    val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override fun toString(): String

    object Keys : KeyRegistry()
}

fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtMemberConfig<*>.findOption(predicate: (CwtOptionConfig) -> Boolean): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && predicate(it) }?.cast()
}

// fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig>? {
//     return optionConfigs?.filter { it is CwtOptionConfig && it.key == key }?.cast()
// }
//
// inline fun CwtMemberConfig<*>.findOptions(predicate: (CwtOptionConfig) -> Boolean): List<CwtOptionConfig> {
//     return optionConfigs?.filter { it is CwtOptionConfig && predicate(it) }.orEmpty().cast()
// }

fun CwtMemberConfig<*>.findOptionValue(value: String): CwtOptionValueConfig? {
    return optionConfigs?.find { it is CwtOptionValueConfig && it.value == value }?.cast()
}
