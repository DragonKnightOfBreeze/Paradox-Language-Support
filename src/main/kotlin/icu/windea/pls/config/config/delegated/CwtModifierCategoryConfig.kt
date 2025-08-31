package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("supported_scopes: string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig?
    }

    companion object : Resolver by CwtModifierCategoryConfigResolverImpl()
}
