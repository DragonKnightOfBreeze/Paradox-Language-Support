package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.util.CwtConfigResolverMixin

internal class CwtExtendedOnActionConfigResolverImpl : CwtExtendedOnActionConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val eventType = config.optionData { eventType }
        if (eventType == null) {
            logger.warn("Skipped invalid extended on action config (name: $name): Missing event_type option.".withLocationPrefix(config))
            return null
        }
        val hint = config.optionData { hint }
        logger.debug { "Resolved extended on action config (name: $name, event type: $eventType).".withLocationPrefix(config) }
        return CwtExtendedOnActionConfigImpl(config, name, eventType, hint)
    }
}

private class CwtExtendedOnActionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val eventType: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedOnActionConfig {
    override fun toString() = "CwtExtendedOnActionConfigImpl(name='$name', eventType='$eventType')"
}
