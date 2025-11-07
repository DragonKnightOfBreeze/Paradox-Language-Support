package icu.windea.pls.ep.configContext

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.declarationConfigContext
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

// region Extensions

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by createKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by createKey(CwtDeclarationConfigContext.Keys)

// endregion

/**
 * 提供基础的声明规则上下文。
 */
class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, configGroup)
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesUsedInDeclaration
        val subtypes = definitionSubtypes?.filter { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.configForDeclaration
        val configs = CwtConfigManipulator.createListForDeepCopy(rootConfig.configs)
        val finalRootConfig = CwtPropertyConfig.delegated(rootConfig, configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        CwtPropertyConfig.postOptimize(finalRootConfig) // 进行后续优化
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
        if (definitionType != ParadoxDefinitionTypes.GameRule) return null
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
        val finalRootConfig = CwtPropertyConfig.delegated(rootConfig, configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        CwtPropertyConfig.postOptimize(finalRootConfig) // 进行后续优化
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
        if (definitionType != ParadoxDefinitionTypes.OnAction) return null
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
        val finalRootConfig = CwtPropertyConfig.delegated(rootConfig, configs)
        finalRootConfig.declarationConfigContext = context
        if (configs != null) configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        CwtPropertyConfig.postOptimize(finalRootConfig) // 进行后续优化
        return finalRootConfig
    }
}
