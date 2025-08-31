package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("aliases: string[]")
    val aliases: Set<@CaseInsensitive String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtScopeConfig?
    }

    companion object : Resolver by CwtScopeConfigResolverImpl()
}
