package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
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
        val gameRuleConfig = context.getUserData(configKey)!!
        val rootConfig = gameRuleConfig.config
        return rootConfig.delegated(CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context), null)
        //parentConfig should be null here
    }
}
