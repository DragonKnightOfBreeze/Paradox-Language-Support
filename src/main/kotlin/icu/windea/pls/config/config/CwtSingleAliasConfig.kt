package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.collections.*

interface CwtSingleAliasConfig : CwtInlineableConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
    val key = config.key
    val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.intern() ?: return null
    return CwtSingleAliasConfigImpl(config, name)
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : CwtSingleAliasConfig {
    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
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
}