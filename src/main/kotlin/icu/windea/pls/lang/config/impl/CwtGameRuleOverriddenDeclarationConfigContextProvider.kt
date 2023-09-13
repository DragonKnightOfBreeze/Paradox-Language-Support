package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

private val configKey = Key.create<CwtGameRuleConfig>("cwt.declarationConfigProvider.gameRule.config")

class CwtGameRuleOverriddenDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //某些game_rule的声明规则需要重载
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "game_rule") return null
        val gameRuleConfig = configGroup.gameRules.get(definitionName) ?: return null
        if(gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
            .apply { putUserData(configKey, gameRuleConfig) }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionName = context.definitionName
        return "gr@$gameTypeId#$definitionName"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        if(!declarationConfig.propertyConfig.isBlock) return declarationConfig.propertyConfig
        
        val gameRuleConfig = context.getUserData(configKey)!!
        val rootConfigs = gameRuleConfig.config.configs!!
        val configs = rootConfigs.flatMap { ParadoxConfigGenerator.deepCopyConfigsInDeclarationConfig(it, context) }
        return declarationConfig.propertyConfig.copy(configs = configs)
        //declarationConfig.propertyConfig.parent should be null here
    }
    
    private fun getGameRuleRootConfigs(definitionName: String?, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>>? {
        if(definitionName == null) return null
        val gameRuleConfig = configGroup.gameRules.get(definitionName) ?: return null
        return gameRuleConfig.config.configs
    }
}
