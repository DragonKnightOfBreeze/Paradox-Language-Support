package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedGameRuleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * @property name template_expression
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    val configForDeclaration: CwtPropertyConfig?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig
    }

    companion object : Resolver by CwtExtendedGameRuleConfigResolverImpl()
}
