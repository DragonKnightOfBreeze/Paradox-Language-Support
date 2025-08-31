package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.stringValue

internal class CwtExtendedOnActionConfigResolverImpl : CwtExtendedOnActionConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfigImpl? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val eventType = config.findOption("event_type")?.stringValue ?: return null
        val hint = config.findOption("hint")?.stringValue
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
