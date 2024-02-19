package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

class CwtSingleAliasConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val config: CwtPropertyConfig,
    override val name: String
) : CwtInlineableConfig<CwtProperty> {
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        //inline all value and configs
        val other = this.config
        val inlined = config.copy(
            value = other.value,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            documentation = config.documentation ?: other.documentation,
            options = config.options
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = config.inlineableConfig //should not set to this - a single alias config do not inline property key  
        return inlined
    }
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtSingleAliasConfig {
            return CwtSingleAliasConfig(config.pointer, config.info, config, name)
        }
    }
}