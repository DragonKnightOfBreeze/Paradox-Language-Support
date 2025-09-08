package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.optionValues
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton

/**
 * 提供了一些扩展方法，以用来方便地得到规则数据。
 */
interface CwtOptionDataAccessorExtensionsAware {
    fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
        return optionConfigs?.findIsInstance<CwtOptionConfig> { it.key == key }
    }

    fun CwtMemberConfig<*>.findOption(vararg keys: String): CwtOptionConfig? {
        return optionConfigs?.findIsInstance<CwtOptionConfig> { it.key in keys }
    }

    fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig> {
        return optionConfigs?.filterIsInstance<CwtOptionConfig> { it.key == key }.orEmpty()
    }

    fun CwtMemberConfig<*>.findOptions(vararg keys: String): List<CwtOptionConfig> {
        return optionConfigs?.filterIsInstance<CwtOptionConfig> { it.key in keys }.orEmpty()
    }

    fun CwtMemberConfig<*>.findOptionValue(value: String): CwtOptionValueConfig? {
        return optionConfigs?.find { it is CwtOptionValueConfig && it.value == value }?.cast()
    }

    fun CwtOptionMemberConfig<*>.getOptionValue(): String? {
        return stringValue
    }

    fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
        return optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue?.intern() }
    }

    fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
        return getOptionValue()?.singleton?.set() ?: getOptionValues()
    }

    companion object : CwtOptionDataAccessorExtensionsAware
}
