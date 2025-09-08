package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtSingleAliasConfigResolverImpl : CwtSingleAliasConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.intern() ?: return null
        return CwtSingleAliasConfigImpl(config, name)
    }
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtSingleAliasConfig {
    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        // inline all value and configs
        val other = this.config
        val inlined = CwtPropertyConfig.copy(
            targetConfig = config,
            value = other.value,
            valueType = other.valueType,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            optionConfigs = config.optionConfigs,
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.singleAliasConfig = this
        return inlined
    }

    override fun toString() = "CwtSingleAliasConfigImpl(name='$name')"
}
