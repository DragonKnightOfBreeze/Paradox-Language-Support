package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedComplexEnumValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedComplexEnumValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig
    }

    companion object : Resolver by CwtExtendedComplexEnumValueConfigResolverImpl()
}
