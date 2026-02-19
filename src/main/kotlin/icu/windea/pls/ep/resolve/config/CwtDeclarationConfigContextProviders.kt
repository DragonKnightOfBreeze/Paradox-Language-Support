package icu.windea.pls.ep.resolve.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.declarationConfigContext
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.lang.resolve.gameRuleConfig
import icu.windea.pls.lang.resolve.onActionConfig
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

/**
 * 提供基础的声明规则上下文。
 */
class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, configGroup)
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val typeString = context.definitionType
        val subtypesString = context.definitionSubtypes?.orNull()?.let { subtypes ->
            val subtypesToDistinct = declarationConfig.subtypesUsedInDeclaration
            buildString {
                for (subtype in subtypes) {
                    if (subtype in subtypesToDistinct) append(".").append(subtype)
                }
            }
        }.orEmpty()
        return "b@$gameTypeId#$typeString$subtypesString"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.configForDeclaration
        val configs = CwtConfigManipulator.createListForDeepCopy(rootConfig.configs)
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclaration(rootConfig, finalRootConfig, context).orEmpty()
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
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.gameRule) return null
        if (definitionName.isNullOrEmpty()) return null
        val gameRuleConfig = configGroup.extendedGameRules.findByPattern(definitionName, element, configGroup) ?: return null
        if (gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, configGroup)
            .apply { this.gameRuleConfig = gameRuleConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionName = context.definitionName
        return "gr@$gameTypeId#$definitionName"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = context.gameRuleConfig?.configForDeclaration ?: declarationConfig.configForDeclaration
        val configs = CwtConfigManipulator.createListForDeepCopy(rootConfig.configs)
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclaration(rootConfig, finalRootConfig, context).orEmpty()
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
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.onAction) return null
        if (definitionName.isNullOrEmpty()) return null
        val onActionConfig = configGroup.extendedOnActions.findByPattern(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, configGroup)
            .apply { this.onActionConfig = onActionConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionName = context.definitionName
        return "oa@$gameTypeId#$definitionName"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.configForDeclaration
        val configs = CwtConfigManipulator.createListForDeepCopy(rootConfig.configs)
        val finalRootConfig = rootConfig.delegated(configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclaration(rootConfig, finalRootConfig, context).orEmpty()
        finalRootConfig.postOptimize() // 进行后续优化
        return finalRootConfig
    }
}
