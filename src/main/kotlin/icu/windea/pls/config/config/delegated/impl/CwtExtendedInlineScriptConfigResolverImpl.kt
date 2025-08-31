package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton

internal class CwtExtendedInlineScriptConfigResolverImpl : CwtExtendedInlineScriptConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfigImpl {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
        return CwtExtendedInlineScriptConfigImpl(config, name, contextConfigsType)
    }
}

private class CwtExtendedInlineScriptConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextConfigsType: String,
) : UserDataHolderBase(), CwtExtendedInlineScriptConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }

    override fun getContainerConfig(): CwtMemberConfig<*> {
        return _containerConfig
    }

    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
        return _contextConfigs
    }

    private fun doGetContainerConfig(): CwtMemberConfig<*> {
        if (config !is CwtPropertyConfig) return config
        // https:// github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/#76
        return CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    private fun doGetContextConfigs(): List<CwtMemberConfig<*>> {
        val containerConfig = _containerConfig
        if (containerConfig !is CwtPropertyConfig) return emptyList()
        val r = when (contextConfigsType) {
            "multiple" -> containerConfig.configs.orEmpty()
            else -> containerConfig.valueConfig.singleton.listOrEmpty()
        }
        if (r.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(config, r, config.configGroup)
        return listOf(contextConfig)
    }

    override fun toString() = "CwtExtendedInlineScriptConfigImpl(name='$name')"
}
