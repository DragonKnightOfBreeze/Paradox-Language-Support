package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

//path: *.cwt#game_rules/*

/**
 * @property name template_expression
 */
interface CwtGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtGameRuleConfig = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtMemberConfig<*>): CwtGameRuleConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    return CwtGameRuleConfigImpl(config, name)
}

private class CwtGameRuleConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String
) : CwtGameRuleConfig

