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

/**
 * 选项数据读取扩展（Extensions Aware）。
 *
 * 概述：
 * - 为规则成员（[CwtMemberConfig]）提供一组便捷方法，以在访问器实现中统一检索选项（[CwtOptionMemberConfig]）。
 * - 常用于 [CwtOptionDataAccessors] 内部以及各类 Resolver 中，作为“底层原语”供上层访问器封装。
 * - 通过这些扩展，可快速获取：单个/多个选项、选项值集合、或将标量/集合统一为集合（`getOptionValueOrValues()`）。
 *
 * 典型用法：
 * - 在具体 accessor 中：`val option = findOption("push_scope")`、`option?.getOptionValue()`。
 * - 在调用侧：`config.optionData { someAccessor }`（详见 [CwtOptionDataAccessors]）。
 *
 * @see CwtOptionDataAccessor
 * @see CwtOptionDataAccessors
 */
interface CwtOptionDataAccessorExtensionsAware {
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

    @Suppress("unused")
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

    @Suppress("unused")
    companion object : CwtOptionDataAccessorExtensionsAware
}
