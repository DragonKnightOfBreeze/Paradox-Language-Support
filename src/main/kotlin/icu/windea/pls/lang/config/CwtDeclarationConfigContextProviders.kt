package icu.windea.pls.lang.config

import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

//region Extensions

var CwtDeclarationConfigContext.gameRuleConfig: CwtGameRuleConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtOnActionConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)

//endregion

class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesToDistinct
        val subtypes = definitionSubtypes?.filter { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.propertyConfig
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context)
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}

class CwtGameRuleOverriddenDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //某些game_rule的声明规则需要重载
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "game_rule") return null
        val gameRuleConfig = configGroup.gameRules.get(definitionName) ?: return null
        if(gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
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
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context)
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}

