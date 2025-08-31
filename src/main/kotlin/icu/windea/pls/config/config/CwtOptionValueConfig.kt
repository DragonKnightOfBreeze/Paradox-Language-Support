package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtType

interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtOption> {
    interface Resolver {
        fun resolve(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        ): CwtOptionValueConfig
    }

    companion object : Resolver by CwtOptionValueConfigResolverImpl()
}
