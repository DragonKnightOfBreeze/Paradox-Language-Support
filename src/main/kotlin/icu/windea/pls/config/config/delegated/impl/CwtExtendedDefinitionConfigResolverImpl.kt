package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.stringValue

class CwtExtendedDefinitionConfigResolverImpl : CwtExtendedDefinitionConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfigImpl? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val type = config.findOption("type")?.stringValue ?: return null
        val hint = config.findOption("hint")?.stringValue
        return CwtExtendedDefinitionConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDefinitionConfig {
    override fun toString() = "CwtExtendedDefinitionConfigImpl(name='$name', type='$type')"
}
