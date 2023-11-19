package icu.windea.pls.lang.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

//region Extensions

var CwtDeclarationConfigContext.gameRuleConfig: CwtGameRuleConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtOnActionConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)

//endregion

class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesToDistinct
        val subtypes = definitionSubtypes?.filterFast { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.propertyConfig
        val configs = if(rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if(configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEachFast { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}

class CwtGameRuleDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //某些game_rule的声明规则需要重载
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "game_rule") return null
        val gameRuleConfig = configGroup.gameRules.get(definitionName) ?: return null
        if(gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.gameRuleConfig = gameRuleConfig }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "gr@$gameTypeId#$definitionName"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val gameRuleConfig = context.gameRuleConfig!!
        val rootConfig = gameRuleConfig.config
        val configs = if(rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if(configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEachFast { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}


class CwtOnActionDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //如果预定义的on_action可以确定事件类型，其声明规则需要经过修改（将其中匹配"<event>"的规则，替换为此事件类型对应的规则）
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "on_action") return null
        val onActionConfig = configGroup.onActions.getByTemplate(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext(definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.onActionConfig = onActionConfig }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "oa@$gameTypeId#$definitionName"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.propertyConfig
        val configs = if(rootConfig.configs == null) null else mutableListOf<CwtMemberConfig<*>>()
        val finalRootConfig = rootConfig.delegated(configs, null)
        finalRootConfig.declarationConfigContext = context
        if(configs == null) return finalRootConfig
        configs += CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, finalRootConfig, context).orEmpty()
        configs.forEachFast { it.parentConfig = finalRootConfig }
        return finalRootConfig
    }
}