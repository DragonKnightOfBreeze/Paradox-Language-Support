package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtConfigInlineMode
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.model.CwtType

object CwtConfigInlineService {
    @Optimized
    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val configGroup = config.configGroup
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return inlineSingleAlias(config, singleAliasConfig)
    }

    @Optimized
    fun inlineSingleAlias(config: CwtPropertyConfig, singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
        // inline all value and configs
        val other = singleAliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = CwtConfigCopyService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigMergeService.mergeOptionData(inlined.optionData, config.optionData, other.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    @Optimized
    fun inlineAlias(config: CwtPropertyConfig, key: String): List<CwtMemberConfig<*>>? {
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.AliasMatchLeft) return null
        val aliasName = valueExpression.value ?: return null
        val configGroup = config.configGroup
        val aliasConfigGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasKeys = CwtConfigManager.getAliasKeys(configGroup, aliasName, key)
        if (aliasKeys.isEmpty()) return emptyList()
        val result = CwtConfigCopyService.createListForDeepCopy()
        aliasKeys.forEach f1@{ aliasKey ->
            val aliasConfigs = aliasConfigGroup[aliasKey]
            if (aliasConfigs.isNullOrEmpty()) return@f1
            aliasConfigs.forEachFast f2@{ aliasConfig ->
                result += inlineAlias(config, aliasConfig) ?: return@f2
            }
        }
        val parentConfig = config.parentConfig
        if (parentConfig != null) CwtConfigService.injectConfigs(parentConfig, result)
        return result
    }

    @Optimized
    fun inlineAlias(config: CwtPropertyConfig, aliasConfig: CwtAliasConfig): CwtPropertyConfig? {
        val other = aliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = aliasConfig.subNameExpression,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = CwtConfigCopyService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigMergeService.mergeOptionData(inlined.optionData, config.optionData, other.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = aliasConfig
        inlined.inlineConfig = config.inlineConfig
        val finalInlined = when (inlined.valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(inlined) ?: return null
            else -> inlined
        }
        return finalInlined
    }

    @Optimized
    fun inlineMacro(macroConfig: CwtMacroConfig.InlineScript): CwtPropertyConfig {
        val other = macroConfig.configForDeclaration
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = other,
            keyExpression = CwtDataExpression.resolveKey(macroConfig.name),
            configs = CwtConfigCopyService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigMergeService.mergeOptionData(inlined.optionData, other.optionData) // merge option data
        inlined.inlineConfig = macroConfig
        return inlined
    }

    @Optimized
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: CwtConfigInlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.keyExpression else return null
                CwtConfigInlineMode.VALUE_TO_KEY -> CwtDataExpression.resolveKey(otherConfig.value)
                else -> config.keyExpression
            },
            valueExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) CwtDataExpression.resolveValue(otherConfig.key) else return null
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueExpression
                else -> config.valueExpression
            },
            valueType = when (inlineMode) {
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueType
                CwtConfigInlineMode.KEY_TO_VALUE -> CwtType.String
                else -> config.valueType
            },
            configs = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> null
                CwtConfigInlineMode.VALUE_TO_VALUE -> CwtConfigCopyService.deepCopyConfigs(otherConfig)
                else -> CwtConfigCopyService.deepCopyConfigs(config)
            },
        )
        inlined.postOptimize() // do post optimization
        CwtConfigMergeService.mergeOptionData(inlined.optionData, config.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    @Optimized
    fun inlineWithConfigs(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        val inlined = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = configGroup,
            valueExpression = CwtDataExpression.resolveBlock(),
            valueType = CwtType.Block,
            configs = configs,
        )
        CwtConfigMergeService.mergeOptionData(inlined.optionData, config?.optionData) // merge option data
        return inlined
    }
}
