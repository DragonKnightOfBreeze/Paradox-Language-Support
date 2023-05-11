package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

class CwtGameRuleDeclarationConfigInjector : CwtDeclarationConfigInjector {
    companion object {
        val configKey = Key.create<CwtGameRuleConfig>("cwt.config.injector.gameRule.config")
    }
    
    //某些game_rule的声明规则需要重载
    
    override fun supports(configContext: CwtConfigContext): Boolean {
        val (_, name, type, _, configGroup, _) = configContext
        if(type == "game_rule") {
            if(name == null) return false
            val config = configGroup.gameRules.get(name)
            configContext.putUserData(configKey, config)
            return config != null
        }
        return false
    }
    
    override fun handleCacheKey(cacheKey: String, configContext: CwtConfigContext): String? {
        val config = configContext.getUserData(configKey)
        if(config == null) return null
        if(doGetDeclarationMergedConfig(config) == null) return cacheKey
        return "${configContext.definitionName}#${cacheKey}"
    }
    
    override fun getDeclarationMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig? {
        val config = configContext.getUserData(configKey) ?: return null
        return doGetDeclarationMergedConfig(config)
    }
    
    private fun doGetDeclarationMergedConfig(config: CwtGameRuleConfig): CwtPropertyConfig? {
        return config.config.takeIf { it.configs.isNotNullOrEmpty() }
    }
}