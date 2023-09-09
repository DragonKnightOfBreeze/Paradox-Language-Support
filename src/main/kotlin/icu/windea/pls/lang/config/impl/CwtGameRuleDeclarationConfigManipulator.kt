package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

private val configKey = Key.create<CwtGameRuleConfig>("cwt.config.injector.gameRule.config")

class CwtGameRuleDeclarationConfigManipulator : CwtDeclarationConfigManipulator {
    //某些game_rule的声明规则需要重载
    
    override fun supports(context: CwtDeclarationConfigContext): Boolean {
        val configGroup = context.configGroup
        if(context.definitionType == "game_rule") {
            if(context.definitionName == null) return false
            val config = configGroup.gameRules.get(context.definitionName)
            context.putUserData(configKey, config)
            return config != null
        }
        return false
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext): String? {
        val config = context.getUserData(configKey)
        if(config == null) return null
        if(doGetDeclarationMergedConfig(config) == null) return null
        val gameTypeId = context.configGroup.gameType.id
        return "$gameTypeId:${context.matchOptions}#game_rule@${context.definitionName}"
    }
    
    override fun getDeclarationMergedConfig(context: CwtDeclarationConfigContext): CwtPropertyConfig? {
        val config = context.getUserData(configKey)
        if(config == null) return null
        return doGetDeclarationMergedConfig(config)
    }
    
    private fun doGetDeclarationMergedConfig(config: CwtGameRuleConfig): CwtPropertyConfig? {
        return config.config.takeIf { it.configs.isNotNullOrEmpty() }
    }
}