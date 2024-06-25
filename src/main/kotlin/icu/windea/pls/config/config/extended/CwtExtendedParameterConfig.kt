package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name (key) template_expression
 * @property contextKey (option) context_key: string
 * @property contextConfigsType (option) context_configs_type: string = "single" ("single" | "multiple")
 * @property inheritConfigContext (option value) inherit_config_context
 * @property inheritScopeContext (option value) inherit_scope_context
 */
interface CwtExtendedParameterConfig : CwtExtendedConfig {
    val name: String
    val contextKey: String
    val contextConfigsType: String
    val inheritScopeContext: Boolean
    val inheritConfigContext: Boolean
    
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
    val inheritConfigContext = config.findOptionValue("inherit_config_context") != null
    val inheritScopeContext = config.findOptionValue("inherit_scope_context") != null
    return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType, inheritConfigContext, inheritScopeContext)
}

private class CwtExtendedParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: String,
    override val inheritConfigContext: Boolean,
    override val inheritScopeContext: Boolean,
) : CwtExtendedParameterConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }
    
    override fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*> {
        if(inheritScopeContext) {
            //TODO 1.3.12
        }
        return _containerConfig
    }
    
    override fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        if(inheritConfigContext) {
            //TODO 1.3.12
        }
        return _contextConfigs
    }
    
    private fun doGetContainerConfig(): CwtMemberConfig<*> {
        return when(config) {
            is CwtPropertyConfig -> CwtConfigManipulator.inlineSingleAlias(config) ?: config // #76
            else -> config
        }
    }
    
    private fun doGetContextConfigs(): List<CwtMemberConfig<*>> {
        val containerConfig = _containerConfig
        if(containerConfig !is CwtPropertyConfig) return emptyList()
        val r = when(contextConfigsType) {
            "multiple" -> containerConfig.configs.orEmpty()
            else -> containerConfig.valueConfig.toSingletonListOrEmpty()
        }
        if(r.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineAsValueConfig(config, r, config.configGroup)
        return listOf(contextConfig)
    }
}
