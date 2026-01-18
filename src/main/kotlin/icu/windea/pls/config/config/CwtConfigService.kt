package icu.windea.pls.config.config

import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.ep.config.config.CwtConfigPostProcessor
import icu.windea.pls.ep.config.config.CwtInjectedConfigProvider

object CwtConfigService {
    /**
     * @see CwtConfigPostProcessor.postProcess
     */
    fun postProcess(config: CwtMemberConfig<*>) {
        val eps = CwtConfigPostProcessor.EP_NAME.extensionList
        for ((_, ep) in eps.withIndex()) {
            if (!ep.supports(config)) continue
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
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        var r = false
        val eps = CwtInjectedConfigProvider.EP_NAME.extensionList
        for ((_, ep) in eps.withIndex()) {
            if (!ep.supports(parentConfig)) continue
            r = r || ep.injectConfigs(parentConfig, configs)
        }
        return r
    }
}
