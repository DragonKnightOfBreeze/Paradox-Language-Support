package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.copy
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtInlineConfigResolverImpl : CwtInlineConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtInlineConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtInlineConfigImpl? {
        val name = config.key.removeSurroundingOrNull("inline[", "]")?.orNull()?.intern() ?: return null
        return CwtInlineConfigImpl(config, name)
    }
}

private class CwtInlineConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtInlineConfig {
    override fun inline(): CwtPropertyConfig {
        val other = this.config
        val inlined = other.copy(
            key = name,
            configs = CwtConfigManipulator.deepCopyConfigs(other)
        )
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = this
        return inlined
    }

    override fun toString() = "CwtInlineConfigImpl(name='$name')"
}
