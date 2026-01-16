@file:Optimized

package icu.windea.pls.config.config.impl

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.Optimized

internal class CwtMemberConfigResolverImpl : CwtMemberConfig.Resolver {
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> delegated(targetConfig: T, configs: List<CwtMemberConfig<*>>?): T {
        return when (targetConfig) {
            is CwtPropertyConfig -> CwtPropertyConfig.delegated(targetConfig, configs)
            is CwtValueConfig -> CwtValueConfig.delegated(targetConfig, configs)
        } as T
    }

    override fun withConfigs(config: CwtMemberConfig<*>, configs: List<CwtMemberConfig<*>>): Boolean {
        return when(config) {
            is CwtPropertyConfig -> CwtPropertyConfig.withConfigs(config, configs)
            is CwtValueConfig -> CwtValueConfig.withConfigs(config, configs)
        }
    }

    override fun postProcess(config: CwtMemberConfig<*>) {
        return when (config) {
            is CwtPropertyConfig -> CwtPropertyConfig.postProcess(config)
            is CwtValueConfig -> CwtValueConfig.postProcess(config)
        }
    }

    override fun postOptimize(config: CwtMemberConfig<*>) {
        return when (config) {
            is CwtPropertyConfig -> CwtPropertyConfig.postOptimize(config)
            is CwtValueConfig -> CwtValueConfig.postOptimize(config)
        }
    }
}
