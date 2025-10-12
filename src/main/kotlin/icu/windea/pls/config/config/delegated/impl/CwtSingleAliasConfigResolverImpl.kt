package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtSingleAliasConfigResolverImpl : CwtSingleAliasConfig.Resolver {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.intern() ?: return null
        logger.debug { "Resolved single alias config (name: $name).".withLocationPrefix(config) }
        return CwtSingleAliasConfigImpl(config, name)
    }
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtSingleAliasConfig {
    override fun toString() = "CwtSingleAliasConfigImpl(name='$name')"
}
