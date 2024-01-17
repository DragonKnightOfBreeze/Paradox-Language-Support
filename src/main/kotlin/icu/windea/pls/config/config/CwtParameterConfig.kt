package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

class CwtParameterConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtMemberElement>,
    override val info: CwtConfigGroupInfo,
    val config: CwtMemberConfig<*>,
    val name: String,
    val contextKey: String,
    val contextConfigsType: String,
) : CwtConfig<CwtMemberElement> {
    /**
     * 得到由其声明的上下文CWT规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>> {
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
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtParameterConfig? {
            val name = when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
            val contextKey = config.findOption("context_key")?.stringValue ?: return null
            val contextConfigsType = config.findOption("context_configs_type")?.stringValue ?: "single"
            return CwtParameterConfig(config.pointer, config.info, config, name, contextKey, contextConfigsType)
        }
    }
}
