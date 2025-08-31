package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.findOptionValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.ep.parameter.containingContextReference
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.script.psi.ParadoxScriptMemberElement

internal class CwtExtendedParameterConfigResolverImpl : CwtExtendedParameterConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfigImpl? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextKey = config.findOption("context_key")?.stringValue ?: return null
        val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
        val inherit = config.findOptionValue("inherit") != null
        return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType, inherit)
    }
}

private class CwtExtendedParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: String,
    override val inherit: Boolean,
) : UserDataHolderBase(), CwtExtendedParameterConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }

    override fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*> {
        return _containerConfig
    }

    override fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        if (inherit) {
            run {
                val contextReferenceElement = parameterElement.containingContextReference?.element ?: return@run
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMemberElement>(false) ?: return@run
                val contextConfigs = ParadoxExpressionManager.getConfigContext(parentElement)?.getConfigs().orEmpty()
                PlsCoreManager.dynamicContextConfigs.set(true)
                return contextConfigs
            }
            return emptyList()
        }
        return _contextConfigs
    }

    private fun doGetContainerConfig(): CwtMemberConfig<*> {
        if (config !is CwtPropertyConfig) return config
        // https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/#76
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

    override fun toString() = "CwtExtendedParameterConfigImpl(name='$name', contextKey='$contextKey')"
}
