package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.optionValues
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.findLastIsInstance
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton

@Suppress("unused")
interface CwtOptionDataExtensions {
    fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
        return optionConfigs?.findLastIsInstance<CwtOptionConfig> { it.key == key }
    }

    fun CwtMemberConfig<*>.findOption(vararg keys: String): CwtOptionConfig? {
        return optionConfigs?.findLastIsInstance<CwtOptionConfig> { it.key in keys }
    }

    fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig> {
        return optionConfigs?.filterIsInstance<CwtOptionConfig> { it.key == key }.orEmpty()
    }

    fun CwtMemberConfig<*>.findOptions(vararg keys: String): List<CwtOptionConfig> {
        return optionConfigs?.filterIsInstance<CwtOptionConfig> { it.key in keys }.orEmpty()
    }

    fun CwtMemberConfig<*>.findOptionValues(): Set<String> {
        return optionConfigs?.filterIsInstance<CwtOptionValueConfig>()?.mapNotNullTo(mutableSetOf()) { it.stringValue?.orNull() }.orEmpty()
    }

    fun CwtOptionMemberConfig<*>.getOptionValue(): String? {
        return stringValue
    }

    fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
        return optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }
    }

    fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
        return getOptionValue()?.singleton?.set() ?: getOptionValues()
    }

    companion object : CwtOptionDataExtensions
}
