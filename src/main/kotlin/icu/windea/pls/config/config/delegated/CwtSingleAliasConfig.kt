package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("single_alias[$]")
    val name: String

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig?
    }

    companion object : Resolver by CwtSingleAliasConfigResolverImpl()
}
