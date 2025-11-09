@file:Optimized

package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.Optimized

internal class CwtMemberConfigResolverImpl : CwtMemberConfig.Resolver {
    override fun postOptimize(config: CwtMemberConfig<*>) {
        return when (config) {
            is CwtPropertyConfig -> CwtPropertyConfig.postOptimize(config)
            is CwtValueConfig -> CwtValueConfig.postOptimize(config)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> delegated(targetConfig: T, configs: List<CwtMemberConfig<*>>?): T {
        return when (targetConfig) {
            is CwtPropertyConfig -> CwtPropertyConfig.delegated(targetConfig, configs)
            is CwtValueConfig -> CwtValueConfig.delegated(targetConfig, configs)
        } as T
    }
}
