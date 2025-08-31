package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSubtypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtSubtypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("subtype[$]")
    val name: String
    @FromOption("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @FromOption("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @FromOption("starts_with: string?")
    val startsWith: String?
    @FromOption("only_if_not: string[]?")
    val onlyIfNot: Set<String>?

    fun inGroup(groupName: String): Boolean

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig?
    }

    companion object : Resolver by CwtSubtypeConfigResolverImpl()
}
