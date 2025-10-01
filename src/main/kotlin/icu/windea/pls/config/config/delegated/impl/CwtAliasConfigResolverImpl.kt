package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtAliasConfigResolverImpl : CwtAliasConfig.Resolver {
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
    override val name = name.intern() // intern to optimize memory
    override val subName = subName.intern() // intern to optimize memory

    override val supportedScopes get() = config.optionData { this.supportedScopes }
    override val outputScope get() = config.optionData { pushScope }

    override val subNameExpression = CwtDataExpression.resolve(subName, true) // cached

    override fun toString() = "CwtAliasConfigImpl(name='$name')"
}
