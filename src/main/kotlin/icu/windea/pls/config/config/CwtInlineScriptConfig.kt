package icu.windea.pls.config.config

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

/**
 * @property name template_expression
 * @property contextConfigsType (option) context_configs_type: string? = "single"
 */
interface CwtInlineScriptConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val contextConfigsType: String
    
    /**
     * 得到由其声明的上下文CWT规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>>
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtInlineScriptConfig = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtInlineScriptConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
    return CwtInlineScriptConfigImpl(config, name, contextConfigsType)
}

private class CwtInlineScriptConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextConfigsType: String,
) : CwtInlineScriptConfig {
    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
        if(config !is CwtPropertyConfig) return emptyList()
        val r = when(contextConfigsType) {
            "multiple" -> config.configs.orEmpty()
            else -> config.valueConfig.toSingletonListOrEmpty()
        }
        if(r.isEmpty()) return emptyList()
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            info = r.first().info,
            value = PlsConstants.blockFolder,
            valueTypeId = CwtType.Block.id,
            configs = r
        )
        return listOf(containerConfig)
    }
}
