package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtSystemScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val id: String
    @FromProperty("base_id: string")
    val baseId: String
    @FromProperty(": string")
    val name: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig
    }

    companion object : Resolver by CwtSystemScopeConfigResolverImpl()
}

