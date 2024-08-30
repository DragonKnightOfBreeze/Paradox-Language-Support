package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property contextConfigsType (option) context_configs_type: string = "single" ("single" | "multiple")
 */
interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val contextConfigsType: String
    
    /**
     * 得到处理后的作为上下文规则的容器的规则。
     */
    fun getContainerConfig(): CwtMemberConfig<*>
    
    /**
     * 得到由其声明的上下文规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>>
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
    return CwtExtendedInlineScriptConfigImpl(config, name, contextConfigsType)
}

private class CwtExtendedInlineScriptConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextConfigsType: String,
) : CwtExtendedInlineScriptConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }
    
    override fun getContainerConfig(): CwtMemberConfig<*> {
        return _containerConfig
    }
    
    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
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
