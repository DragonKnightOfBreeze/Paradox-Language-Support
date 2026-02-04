package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.ep.resolve.parameter.containingContextReference
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

internal class CwtExtendedParameterConfigResolverImpl : CwtExtendedParameterConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextKey = config.optionData.contextKey
        if (contextKey == null) {
            logger.warn("Skipped invalid extended parameter config (name: $name): Missing context_key option.".withLocationPrefix(config))
            return null
        }
        val contextConfigsType = config.optionData.contextConfigsType
        val inherit = config.optionData.inherit
        logger.debug { "Resolved extended parameter config (name: $name, context key: $contextKey).".withLocationPrefix(config) }
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
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMember>(false) ?: return@run
                val contextConfigs = ParadoxConfigManager.getConfigContext(parentElement)?.getConfigs().orEmpty()
                ParadoxConfigService.getResolvingConfigContext()?.dynamic = true // NOTE 2.1.2 需要把正在解析的规则上下文标记为动态的
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
            // "single" -> containerConfig.valueConfig.singleton.listOrEmpty()
            else -> containerConfig.valueConfig.to.singletonListOrEmpty()
        }
        if (r.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(config, r, config.configGroup)
        return listOf(contextConfig)
    }

    override fun toString() = "CwtExtendedParameterConfigImpl(name='$name', contextKey='$contextKey')"
}
