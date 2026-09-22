package icu.windea.pls.ep.resolve.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.lang.resolve.declarationConfigContext
import icu.windea.pls.lang.resolve.gameRuleConfig
import icu.windea.pls.lang.resolve.onActionConfig
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

/**
 * 提供基础的声明规则上下文。
 */
class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?): CwtDeclarationConfigContext {
        if (definitionName == null) return CwtDeclarationConfigContext.create(configGroup, definitionType, definitionSubtypes, this)
        return CwtDeclarationConfigContext.createNamed(configGroup, definitionName, definitionType, definitionSubtypes, this)
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameType = context.gameType
        val typeString = context.definitionType
        return buildString {
            append(gameType.ordinal)
            append("@b@")
            append(typeString)
            context.definitionSubtypes?.orNull()?.let { subtypes ->
                val subtypesToDistinct = declarationConfig.attributes.involvedSubtypes
                subtypes.forEachFast { subtype ->
                    if (subtype in subtypesToDistinct) {
                        append('.').append(subtype)
                    }
                }
            }
        }
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.rootConfig
        val childConfigs = rootConfig.configs
        val configs = if (childConfigs != null) CwtConfigManipulationService.createListForDeepCopy(expectedSize = childConfigs.size) else null
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulationService.deepCopyConfigsBySubtypeExpression(rootConfig, finalRootConfig, context.definitionSubtypes).orEmpty()
        finalRootConfig.postOptimize() // 进行后续优化
        return finalRootConfig
    }
}

/**
 * 提供 game rule 的重载后的声明规则上下文。
 *
 * 如果通过 [CwtExtendedGameRuleConfig] 重载了 game rule 的声明规则，则需使用重载后的声明规则上下文。
 */
class CwtGameRuleDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.gameRule) return null
        if (definitionName.isNullOrEmpty()) return null
        val gameRuleConfig = configGroup.extendedGameRules.findByPattern(definitionName, element, configGroup) ?: return null
        if (gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext.createNamed(configGroup, definitionName, definitionType, definitionSubtypes, this)
            .apply { this.gameRuleConfig = gameRuleConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameType = context.gameType
        val definitionName = context.definitionName
        return buildString {
            append(gameType.ordinal)
            append("@gr@")
            append(definitionName)
        }
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = context.gameRuleConfig?.rootConfig ?: declarationConfig.rootConfig
        val childConfigs = rootConfig.configs
        val configs = if (childConfigs != null) CwtConfigManipulationService.createListForDeepCopy(expectedSize = childConfigs.size) else null
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulationService.deepCopyConfigsBySubtypeExpression(rootConfig, finalRootConfig, context.definitionSubtypes).orEmpty()
        finalRootConfig.postOptimize() // 进行后续优化
        return finalRootConfig
    }
}

/**
 * 提供 on action 的修改后的声明规则上下文。
 *
 * 如果通过 [CwtExtendedOnActionConfig] 可以确定 on action 的事件类型，则需使用修改后的声明规则上下文。
 * 将其中的数据表达式 `<event>`，替换为此事件类型对应的数据表达式。
 */
class CwtOnActionDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.onAction) return null
        if (definitionName.isNullOrEmpty()) return null
        val onActionConfig = configGroup.extendedOnActions.findByPattern(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext.createNamed(configGroup, definitionName, definitionType, definitionSubtypes, this)
            .apply { this.onActionConfig = onActionConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameType = context.gameType
        val definitionName = context.definitionName
        return buildString {
            append(gameType.ordinal)
            append("@oa@")
            append(definitionName)
        }
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.rootConfig
        val childConfigs = rootConfig.configs
        val configs = if (childConfigs != null) CwtConfigManipulationService.createListForDeepCopy(expectedSize = childConfigs.size) else null
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulationService.deepCopyConfigsBySubtypeExpression(rootConfig, finalRootConfig, context.definitionSubtypes).orEmpty()
        finalRootConfig.postOptimize() // 进行后续优化
        return finalRootConfig
    }
}
