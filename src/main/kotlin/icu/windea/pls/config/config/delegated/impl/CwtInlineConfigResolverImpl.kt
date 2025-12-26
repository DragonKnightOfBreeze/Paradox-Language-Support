package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtInlineConfigResolverImpl : CwtInlineConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtInlineConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtInlineConfig? {
        val name = config.key.removeSurroundingOrNull("inline[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid inline config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved inline config (name: $name).".withLocationPrefix(config) }
        return CwtInlineConfigImpl(config, name)
    }
}

private class CwtInlineConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtInlineConfig {
    override fun toString() = "CwtInlineConfigImpl(name='$name')"
}
