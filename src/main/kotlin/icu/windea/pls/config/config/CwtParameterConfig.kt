package icu.windea.pls.config.config

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

/**
 * @property name (key) template_expression
 * @property contextKey (option) context_key: string
 * @property contextConfigsType (option) context_configs_type: string = "single"
 */
interface CwtParameterConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val contextKey: String
    val contextConfigsType: String
    
    /**
     * 得到由其声明的上下文CWT规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>>
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtParameterConfig? {
            return doResolve(config)
        }
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtParameterConfig? {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val contextKey = config.findOption("context_key")?.stringValue ?: return null
    val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
    return CwtParameterConfigImpl(config, name, contextKey, contextConfigsType)
}

private class CwtParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: String,
) : CwtParameterConfig {
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
