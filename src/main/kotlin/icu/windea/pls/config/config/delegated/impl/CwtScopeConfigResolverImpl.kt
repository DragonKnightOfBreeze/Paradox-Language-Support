package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimizedIfEmpty

internal class CwtScopeConfigResolverImpl : CwtScopeConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtScopeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid scope config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        val aliases = propElements.find { it.key == "aliases" }?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimizedIfEmpty().orEmpty()
        logger.debug { "Resolved scope config (name: $name).".withLocationPrefix(config) }
        return CwtScopeConfigImpl(config, name, aliases)
    }
}

private class CwtScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val aliases: Set<String>
) : UserDataHolderBase(), CwtScopeConfig {
    override fun toString() = "CwtScopeConfigImpl(name='$name')"
}
