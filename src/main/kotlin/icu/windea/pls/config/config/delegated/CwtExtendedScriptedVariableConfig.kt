package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedScriptedVariableConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedScriptedVariableConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig?
    }

    companion object : Resolver by CwtExtendedScriptedVariableConfigResolverImpl()
}
