@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.impl.CwtInlineConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("inline[$]")
    val name: String

    fun inline(): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig?
    }

    companion object : Resolver by CwtInlineConfigResolverImpl()
}
