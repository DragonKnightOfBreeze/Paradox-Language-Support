package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

class CwtGameRuleDeclarationConfigInjector : CwtDeclarationConfigInjector {
    companion object {
        val configKey = Key.create<CwtGameRuleConfig?>("cwt.config.injector.gameRule.config")
    }
    
    //某些game_rule的声明规则需要重载
    
    override fun supports(configContext: CwtConfigContext): Boolean {
        val (_, name, type, _, configGroup, matchType) = configContext
        if(type == "game_rule") {
            if(name == null) return false
            val config = configGroup.onActions.get(name)
            return config != null
        }
        return false
    }
    
    override fun getDeclarationMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig? {
        val config = configContext.getUserData(configKey)
        return config?.config?.takeIf { it.configs.isNotNullOrEmpty() }
    }
}