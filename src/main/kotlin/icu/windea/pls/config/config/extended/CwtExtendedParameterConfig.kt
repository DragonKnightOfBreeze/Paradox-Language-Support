package icu.windea.pls.config.config.extended

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * @property name (key) template_expression
 * @property contextKey (option) context_key: string
 * @property contextConfigsType (option) context_configs_type: string = "single" ("single" | "multiple")
 * @property inherit (option value) inherit
 */
interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val contextKey: String
    val contextConfigsType: String
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

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
    val name = when(config) {
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
        if(inherit) {
            run {
                val contextReferenceElement = parameterElement.containingContextReference?.element ?: return@run
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMemberElement>(false) ?: return@run
                val contextConfigs = ParadoxExpressionManager.getConfigContext(parentElement)?.getConfigs().orEmpty()
                return contextConfigs
            }
            return emptyList()
        }
        return _contextConfigs
    }
    
    private fun doGetContainerConfig(): CwtMemberConfig<*> {
        return CwtConfigManipulator.inlineSingleAlias(config) ?: config // #76
    }
    
    private fun doGetContextConfigs(): List<CwtMemberConfig<*>> {
        val containerConfig = _containerConfig
        if(containerConfig !is CwtPropertyConfig) return emptyList()
        val r = when(contextConfigsType) {
            "multiple" -> containerConfig.configs.orEmpty()
            else -> containerConfig.valueConfig.toSingletonListOrEmpty()
        }
        if(r.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(config, r, config.configGroup)
        return listOf(contextConfig)
    }
}
