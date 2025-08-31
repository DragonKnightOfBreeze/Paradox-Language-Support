package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDefinitionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("type: string")
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig?
    }

    companion object : Resolver by CwtExtendedDefinitionConfigResolverImpl()
}

