package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.copy
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.pushScope
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.config.supportedScopes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtAliasConfigResolverImpl: CwtAliasConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtAliasConfigImpl? {
        val key = config.key
        val tokens = key.removeSurroundingOrNull("alias[", "]")?.orNull()
            ?.split(':', limit = 2)?.takeIf { it.size == 2 }
            ?: return null
        val (name, subName) = tokens
        return CwtAliasConfigImpl(config, name, subName)
    }
}

private class CwtAliasConfigImpl(
    override val config: CwtPropertyConfig,
    name: String,
    subName: String
) : UserDataHolderBase(), CwtAliasConfig {
    override val name = name.intern() //intern to optimize memory
    override val subName = subName.intern() //intern to optimize memory

    override val supportedScopes get() = config.supportedScopes
    override val outputScope get() = config.pushScope

    //not much memory will be used, so cached
    override val subNameExpression: CwtDataExpression = CwtDataExpression.resolve(subName, true)

    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        val other = this.config
        val inlined = config.copy(
            key = subName,
            value = other.value,
            valueType = other.valueType,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            optionConfigs = other.optionConfigs
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = this
        inlined.singleAliasConfig = config.singleAliasConfig
        return inlined
    }

    override fun toString() = "CwtAliasConfigImpl(name='$name')"
}
