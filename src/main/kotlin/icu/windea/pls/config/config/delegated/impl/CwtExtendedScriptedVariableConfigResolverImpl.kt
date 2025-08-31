package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.stringValue

internal class CwtExtendedScriptedVariableConfigResolverImpl : CwtExtendedScriptedVariableConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfigImpl {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.findOption("hint")?.stringValue
        return CwtExtendedScriptedVariableConfigImpl(config, name, hint)
    }
}

private class CwtExtendedScriptedVariableConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedScriptedVariableConfig {
    override fun toString() = "CwtExtendedScriptedVariableConfigImpl(name='$name')"
}
