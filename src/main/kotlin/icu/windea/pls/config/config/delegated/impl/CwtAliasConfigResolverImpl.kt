package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtAliasConfigResolverImpl : CwtAliasConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtAliasConfig? {
        val key = config.key
        val tokens = key.removeSurroundingOrNull("alias[", "]")?.orNull()
            ?.split(':', limit = 2)?.takeIf { it.size == 2 }
            ?: return null
        val name = tokens[0].optimized()
        val subName = tokens[1].optimized()
        logger.debug { "Resolved alias config (name: $name, subName: $subName).".withLocationPrefix(config) }
        return CwtAliasConfigImpl(config, name, subName)
    }

    override fun postProcess(config: CwtAliasConfig) {
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(config, config.configExpression)
    }
}

private class CwtAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val subName: String
) : UserDataHolderBase(), CwtAliasConfig {
    override val supportedScopes get() = config.optionData { supportedScopes }
    override val outputScope get() = config.optionData { pushScope }
    override val subNameExpression = CwtDataExpression.resolve(subName, true) // cached
    override val configExpression: CwtDataExpression get() = subNameExpression

    override fun toString() = "CwtAliasConfigImpl(name='$name', subName='$subName')"
}
