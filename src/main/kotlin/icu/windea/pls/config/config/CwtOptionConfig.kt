package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType

    interface Resolver {
        fun resolve(
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig
    }

    companion object : Resolver by CwtOptionConfigResolverImpl()
}
