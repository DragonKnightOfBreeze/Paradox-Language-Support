package icu.windea.pls.config.config

import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.config.config.CwtConfigPostProcessor
import icu.windea.pls.ep.config.config.CwtInjectedConfigProvider

object CwtConfigService {
    /**
     * @see CwtConfigPostProcessor.postProcess
     */
    @Optimized
    fun postProcess(config: CwtMemberConfig<*>) {
        val eps = CwtConfigPostProcessor.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!ep.supports(config)) return@f
            if (ep.deferred(config)) {
                val deferredActions = CwtConfigResolverManager.getPostProcessActions(config.configGroup)
                deferredActions += Runnable { ep.postProcess(config) }
            } else {
                ep.postProcess(config)
            }
        }
    }

    /**
     * @see CwtInjectedConfigProvider.injectConfigs
     */
    @Optimized
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        var r = false
        val eps = CwtInjectedConfigProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!ep.supports(parentConfig)) return@f
            r = r || ep.injectConfigs(parentConfig, configs)
        }
        return r
    }
}
