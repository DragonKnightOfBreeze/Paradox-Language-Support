package icu.windea.pls.config.config.extended

import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * @property name (key) template_expression
 * @property contextKey (option) context_key: string
 * @property contextConfigsType (option) context_configs_type: string = "single"
 */
interface CwtExtendedParameterConfig : CwtExtendedConfig {
    val name: String
    val contextKey: String
    val contextConfigsType: String
    
    /**
     * 得到由其声明的上下文CWT规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>>
    
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
    return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType)
}

private class CwtExtendedParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: String,
) : CwtExtendedParameterConfig {
    private val _contextConfigs by lazy { doGetContextConfigs() }
    
    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
        return _contextConfigs
    }
    
    private fun doGetContextConfigs(): List<CwtMemberConfig<*>> {
        if(config !is CwtPropertyConfig) return emptyList()
        val config = CwtConfigManipulator.inlineSingleAlias(config) ?: config // #76
        val r = when(contextConfigsType) {
            "multiple" -> config.configs.orEmpty()
            else -> config.valueConfig.toSingletonListOrEmpty()
        }
        if(r.isEmpty()) return emptyList()
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = r.first().configGroup,
            value = PlsConstants.Folders.block,
            valueTypeId = CwtType.Block.id,
            configs = r,
            options = config.options,
            documentation = config.documentation
        )
        return listOf(containerConfig)
    }
}
