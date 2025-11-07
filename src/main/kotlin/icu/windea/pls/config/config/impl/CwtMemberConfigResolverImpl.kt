package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig

internal class CwtMemberConfigResolverImpl: CwtMemberConfig.Resolver {
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> delegated(targetConfig: T, configs: List<CwtMemberConfig<*>>?): T {
        return when (targetConfig) {
            is CwtPropertyConfig -> CwtPropertyConfig.delegated(targetConfig, configs)
            is CwtValueConfig -> CwtValueConfig.delegated(targetConfig, configs)
        } as T
    }
}
