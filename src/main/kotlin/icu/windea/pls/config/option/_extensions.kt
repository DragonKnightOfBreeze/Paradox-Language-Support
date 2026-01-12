package icu.windea.pls.config.option

import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton

fun CwtOptionMemberConfig<*>.getOptionValue(): String? {
    return stringValue
}

fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
    return optionConfigs?.mapNotNull { it.castOrNull<CwtOptionValueConfig>()?.stringValue }?.toSet()
}

fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
    return getOptionValue()?.singleton?.set() ?: getOptionValues()
}
