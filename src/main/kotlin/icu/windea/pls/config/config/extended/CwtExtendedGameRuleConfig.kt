package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    return CwtExtendedGameRuleConfigImpl(config, name)
}

private class CwtExtendedGameRuleConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String
) : CwtExtendedGameRuleConfig

