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