@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
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
