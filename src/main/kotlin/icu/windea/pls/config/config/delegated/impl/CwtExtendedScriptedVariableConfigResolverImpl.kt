package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix

internal class CwtExtendedScriptedVariableConfigResolverImpl : CwtExtendedScriptedVariableConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.optionData.hint
        logger.debug { "Resolved extended scripted variable config (name: $name).".withLocationPrefix(config) }
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
