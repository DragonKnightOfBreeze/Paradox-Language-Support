package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

interface CwtInlineConfig : CwtInlineableConfig<CwtProperty, CwtPropertyConfig> {
    override val config: CwtPropertyConfig
    
    val name: String
    
    fun inline(): CwtPropertyConfig
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtInlineConfig? {
    val key = config.key
    val name = key.removeSurroundingOrNull("inline[", "]")?.orNull()?.intern() ?: return null
    return CwtInlineConfigImpl(config, name)
}

private class CwtInlineConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : CwtInlineConfig {
    override fun inline(): CwtPropertyConfig {
        val other = config
        val inlined = other.copy(
            key = name,
            configs = CwtConfigManipulator.deepCopyConfigs(other)
        )
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = this
        return inlined
    }
}