package icu.windea.pls.ep.configContext

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDeclarationConfig
import icu.windea.pls.config.config.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.declarationConfigContext
import icu.windea.pls.config.config.delegated
import icu.windea.pls.config.config.subtypesUsedInDeclaration
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

//region Extensions

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by createKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by createKey(CwtDeclarationConfigContext.Keys)

//endregion

class BaseCwtDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesUsedInDeclaration
        val subtypes = definitionSubtypes?.filter { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.configForDeclaration
        val configs = if (rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if (configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEach { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}

class GameRuleCwtDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //某些game_rule的声明规则需要重载

    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.GameRule) return null
        if (definitionName.isNullOrEmpty()) return null
        val gameRuleConfig = configGroup.extendedGameRules.findFromPattern(definitionName, element, configGroup) ?: return null
        if (gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.gameRuleConfig = gameRuleConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "gr@$gameTypeId#$definitionName"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        //如果存在，使用重载的声明规则
        val rootConfig = context.gameRuleConfig?.configForDeclaration ?: declarationConfig.configForDeclaration
        val configs = if (rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if (configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEach { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}

class OnActionCwtDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //如果预定义的on_action可以确定事件类型，其声明规则需要经过修改（将其中匹配"<event>"的规则，替换为此事件类型对应的规则）

    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if (definitionType != ParadoxDefinitionTypes.OnAction) return null
        if (definitionName.isNullOrEmpty()) return null
        val onActionConfig = configGroup.extendedOnActions.findFromPattern(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.onActionConfig = onActionConfig }
    }

    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "oa@$gameTypeId#$definitionName"
    }

    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.configForDeclaration
        val configs = if (rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if (configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEach { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}
