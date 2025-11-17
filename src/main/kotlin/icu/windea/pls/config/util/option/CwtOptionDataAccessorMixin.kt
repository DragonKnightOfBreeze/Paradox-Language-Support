package icu.windea.pls.config.util.option

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton

@Optimized
@Suppress("unused")
interface CwtOptionDataAccessorMixin {
    fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
        val optionConfigs = optionConfigs
        if (optionConfigs.isEmpty()) return null
        optionConfigs.forEachReversedFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            if (optionConfig.key != key) return@f
            return optionConfig
        }
        return null
    }

    fun CwtMemberConfig<*>.findOption(vararg keys: String): CwtOptionConfig? {
        val optionConfigs = optionConfigs
        if (optionConfigs.isEmpty()) return null
        optionConfigs.forEachReversedFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            if (optionConfig.key !in keys) return@f
            return optionConfig
        }
        return null
    }

    fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig> {
        val optionConfigs = optionConfigs
        if (optionConfigs.isEmpty()) return emptyList()
        val result = FastList<CwtOptionConfig>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            if (optionConfig.key != key) return@f
            result.add(optionConfig)
        }
        return result
    }

    fun CwtMemberConfig<*>.findOptions(vararg keys: String): List<CwtOptionConfig> {
        val optionConfigs = optionConfigs
        if (optionConfigs.isEmpty()) return emptyList()
        val result = FastList<CwtOptionConfig>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            if (optionConfig.key !in keys) return@f
            result.add(optionConfig)
        }
        return result
    }

    fun CwtOptionMemberConfig<*>.getOptionValue(): String? {
        return stringValue
    }

    fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
        val optionConfigs = optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptySet()
        val result = FastSet<String>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionValueConfig) return@f
            val v = optionConfig.stringValue ?: return@f
            result.add(v)
        }
        return result
    }

    fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
        return getOptionValue()?.singleton?.set() ?: getOptionValues()
    }
}
