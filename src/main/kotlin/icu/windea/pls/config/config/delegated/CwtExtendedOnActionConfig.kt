@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.impl.CwtExtendedOnActionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedOnActionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("event_type: string")
    val eventType: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig?
    }

    companion object : Resolver by CwtExtendedOnActionConfigResolverImpl()
}
