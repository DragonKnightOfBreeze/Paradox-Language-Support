package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigManipulator

internal class CwtExtendedGameRuleConfigResolverImpl : CwtExtendedGameRuleConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfigImpl {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.findOption("hint")?.stringValue
        return CwtExtendedGameRuleConfigImpl(config, name, hint)
    }
}

private class CwtExtendedGameRuleConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedGameRuleConfig {
    override val configForDeclaration: CwtPropertyConfig? by lazy {
        if (config !is CwtPropertyConfig) return@lazy null
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    override fun toString() = "CwtExtendedGameRuleConfigImpl(name='$name')"
}
