package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypes
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
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.model.type.CwtExpressionType

object CwtConfigInlineService {
    fun inlineAlias(config: CwtPropertyConfig, aliasConfig: CwtAliasConfig): CwtPropertyConfig? {
        val other = aliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = aliasConfig.subNameExpression,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = CwtConfigManipulationService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigManipulationService.mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = aliasConfig
        inlined.inlineConfig = config.inlineConfig
        val finalInlined = when (inlined.valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(inlined) ?: return null
            else -> inlined
        }
        return finalInlined
    }

    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.metadata.value ?: return null
        val configGroup = config.configGroup
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return inlineSingleAlias(config, singleAliasConfig)
    }

    fun inlineSingleAlias(config: CwtPropertyConfig, singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
        // inline all value and configs
        val other = singleAliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = CwtConfigManipulationService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigManipulationService.mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineMacro(macroConfig: CwtMacroConfig.InlineScript): CwtPropertyConfig {
        val other = macroConfig.contextContainerConfig
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = other,
            keyExpression = CwtDataExpression.resolve(macroConfig.name, CwtDataExpressionRole.Key),
            configs = CwtConfigManipulationService.deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        CwtConfigManipulationService.mergeOptionMetadata(inlined.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.inlineConfig = macroConfig
        return inlined
    }

    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: CwtConfigInlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.keyExpression else return null
                CwtConfigInlineMode.VALUE_TO_KEY -> CwtDataExpression.resolve(otherConfig.value, CwtDataExpressionRole.Key)
                else -> config.keyExpression
            },
            valueExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) CwtDataExpression.resolve(otherConfig.key, CwtDataExpressionRole.Value) else return null
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueExpression
                else -> config.valueExpression
            },
            valueType = when (inlineMode) {
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueType
                CwtConfigInlineMode.KEY_TO_VALUE -> CwtExpressionType.String
                else -> config.valueType
            },
            configs = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> null
                CwtConfigInlineMode.VALUE_TO_VALUE -> CwtConfigManipulationService.deepCopyConfigs(otherConfig)
                else -> CwtConfigManipulationService.deepCopyConfigs(config)
            },
        )
        inlined.postOptimize() // do post optimization
        CwtConfigManipulationService.mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineForConfig(config: CwtPropertyConfig): CwtPropertyConfig {
        // #76
        return inlineSingleAlias(config) ?: config
    }

    fun inlineForConfig(config: CwtMemberConfig<*>): CwtMemberConfig<*> {
        // #76
        if (config is CwtPropertyConfig) return inlineSingleAlias(config) ?: config
        return config
    }

    fun inlineForContextConfig(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        val inlined = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = configGroup,
            valueExpression = CwtDataExpression.resolveBlock(),
            valueType = CwtExpressionType.Block,
            configs = configs,
        )
        CwtConfigManipulationService.mergeOptionMetadata(inlined.optionMetadata, config?.optionMetadata) // merge option metadata
        return inlined
    }
}
