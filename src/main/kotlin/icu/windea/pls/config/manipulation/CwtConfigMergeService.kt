package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtConfigExpressionService
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.isSamePointer
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigKeyManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.model.CwtType

object CwtConfigMergeService {
    fun mergeConfigs(configs1: List<CwtMemberConfig<*>>, configs2: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        if (configs1.isEmpty() && configs2.isEmpty()) return emptyList()
        if (configs1.isEmpty()) return configs2
        if (configs2.isEmpty()) return configs1

        if (configs1.size == 1 && configs2.size == 1) {
            val c1 = configs1.single()
            val c2 = configs2.single()
            if (c1 is CwtValueConfig && c2 is CwtValueConfig) {
                if (c1.valueType == CwtType.Block && c2.valueType == CwtType.Block) {
                    val mergedConfigs = mergeConfigs(c1.configs.orEmpty(), c2.configs.orEmpty())
                    return listOf(CwtConfigInlineService.inlineWithConfigs(null, mergedConfigs, c1.configGroup))
                }
                val mergedConfig = mergeValueConfig(c1, c2)
                if (mergedConfig != null) return mergedConfig.to.singletonList()
            } else if (c1 is CwtPropertyConfig && c2 is CwtPropertyConfig) {
                val same = CwtConfigKeyManager.getDistinctKey(c1) == CwtConfigKeyManager.getDistinctKey(c2)
                if (same) return c1.to.singletonList()
            } else {
                return emptyList()
            }
        }

        if (configs1.all { it is CwtValueConfig } && configs2.all { it is CwtValueConfig }) {
            val c1 = when {
                configs1.size == 1 -> configs1.single()
                configs2.size == 1 -> configs2.single()
                else -> null
            }?.castOrNull<CwtValueConfig>()
            val cs2 = when {
                configs1.size == 1 -> configs2
                configs2.size == 1 -> configs1
                else -> null
            }?.castOrNull<List<CwtValueConfig>>()
            if (c1 != null && cs2.isNotNullOrEmpty()) {
                val mergedConfigs = cs2.mapNotNull { c2 -> mergeValueConfig(c1, c2) }
                return mergedConfigs
            }
        }

        val m1 = configs1.associateBy { CwtConfigKeyManager.getDistinctKey(it) }
        val m2 = configs2.associateBy { CwtConfigKeyManager.getDistinctKey(it) }
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        return sameConfigs
    }

    @Suppress("unused")
    fun mergeConfig(config1: CwtMemberConfig<*>, config2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if (config1 === config2) return config1 // reference equality
        if (config1 isSamePointer config2) return config1 // pointer equality
        if (CwtConfigKeyManager.getDistinctKey(config1) == CwtConfigKeyManager.getDistinctKey(config2)) return config1 // distinct key equality
        return null
    }

    fun mergeValueConfig(config1: CwtValueConfig, config2: CwtValueConfig): CwtValueConfig? {
        if (config1 === config2) return config1 // reference equality
        if (config1 isSamePointer config2) return config1 // pointer equality
        if (config1.configExpression.type == CwtDataTypes.Block || config2.configExpression.type == CwtDataTypes.Block) return null // cannot merge non-same clauses
        val expressionString = CwtConfigExpressionService.merge(config1.configExpression, config2.configExpression, config1.configGroup)
        if (expressionString == null) return null
        val merged = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = config1.configGroup,
            valueExpression = CwtDataExpression.resolveValue(expressionString),
        )
        mergeOptionData(merged.optionData, config1.optionData, config2.optionData) // merge option data
        return merged
    }

    fun mergeAndMatchValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        for (config in configs) {
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = CwtConfigExpressionService.merge(e1, e2, config.configGroup) ?: continue // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    fun mergeOptionData(optionData: CwtOptionDataHolder, vararg sources: CwtOptionDataHolder?) {
        for (source in sources) {
            if (source == null) continue
            source.copyTo(optionData)
        }
    }
}
