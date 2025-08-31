@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromOption
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
