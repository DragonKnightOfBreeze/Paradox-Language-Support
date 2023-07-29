package icu.windea.pls.lang.cwt.impl

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*

private val configKey = Key.create<CwtGameRuleConfig>("cwt.config.injector.gameRule.config")

class CwtGameRuleDeclarationConfigInjector : CwtDeclarationConfigInjector {
    //某些game_rule的声明规则需要重载
    
    override fun supports(configContext: CwtDeclarationConfigContext): Boolean {
        val configGroup = configContext.configGroup
        if(configContext.definitionType == "game_rule") {
            if(configContext.definitionName == null) return false
            val config = configGroup.gameRules.get(configContext.definitionName)
            configContext.putUserData(configKey, config)
            return config != null
        }
        return false
    }
    
    override fun getCacheKey(configContext: CwtDeclarationConfigContext): String? {
        val config = configContext.getUserData(configKey)
        if(config == null) return null
        if(doGetDeclarationMergedConfig(config) == null) return null
        return "@game_rule#${configContext.definitionName}#${configContext.matchOptions}"
    }
    
    override fun getDeclarationMergedConfig(configContext: CwtDeclarationConfigContext): CwtPropertyConfig? {
        val config = configContext.getUserData(configKey)
        if(config == null) return null
        return doGetDeclarationMergedConfig(config)
    }
    
    private fun doGetDeclarationMergedConfig(config: CwtGameRuleConfig): CwtPropertyConfig? {
        return config.config.takeIf { it.configs.isNotNullOrEmpty() }
    }
}