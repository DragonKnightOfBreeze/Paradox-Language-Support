@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromOption
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDynamicValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedDynamicValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig
    }

    companion object : Resolver by CwtExtendedDynamicValueConfigResolverImpl()
}
