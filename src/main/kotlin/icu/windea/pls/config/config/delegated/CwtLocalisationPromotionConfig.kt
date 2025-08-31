package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationPromotionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtLocalisationPromotionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig
    }

    companion object : Resolver by CwtLocalisationPromotionConfigResolverImpl()
}
