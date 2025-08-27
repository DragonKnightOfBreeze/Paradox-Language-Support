@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtConfig.Option
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.ep.parameter.containingContextReference
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.script.psi.ParadoxScriptMemberElement

interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @Option("context_key: string")
    val contextKey: String
    @Option("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String
    @Option("inherit")
    val inherit: Boolean

    /**
     * 得到处理后的作为上下文规则的容器的规则。
     */
    fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*>

    /**
     * 得到由其声明的上下文规则列表。
     */
    fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>>

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
            return doResolve(config)
        }
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val contextKey = config.findOption("context_key")?.stringValue ?: return null
    val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
    val inherit = config.findOptionValue("inherit") != null
    return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType, inherit)
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
        //https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/#76
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

    override fun toString(): String {
        return "CwtExtendedParameterConfigImpl(name='$name', contextKey='$contextKey')"
    }
}
