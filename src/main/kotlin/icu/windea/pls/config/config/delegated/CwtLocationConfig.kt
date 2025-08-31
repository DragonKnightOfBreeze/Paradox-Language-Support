package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtLocationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val key: String
    @FromProperty(": string")
    val value: String
    @FromOption("required")
    val required: Boolean
    @FromOption("primary")
    val primary: Boolean

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig?
    }

    companion object : Resolver by CwtLocationConfigResolverImpl()
}
