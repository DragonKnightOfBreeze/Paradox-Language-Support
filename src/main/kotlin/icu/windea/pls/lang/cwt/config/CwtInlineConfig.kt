package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

class CwtInlineConfig(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val config: CwtPropertyConfig,
    override val name: String
) : CwtInlineableConfig<CwtProperty> {
    fun inline(): CwtPropertyConfig {
        //don't cache here
        return doInline()
    }
    
    private fun doInline(): CwtPropertyConfig {
        val other = this.config
        val inlined = other.copy(
            key = this.name,
            configs = ParadoxConfigGenerator.deepCopyConfigs(other)
        )
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = this
        return inlined
    }
}