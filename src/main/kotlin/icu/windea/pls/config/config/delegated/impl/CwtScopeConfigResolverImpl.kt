package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized

internal class CwtScopeConfigResolverImpl : CwtScopeConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtScopeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements == null) {
            logger.warn("Skipped invalid scope config (name: $name): Null properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propElements.groupBy { it.key }
        val aliases = propGroup.getOne("aliases")?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimized().orEmpty()
        val isSubscopeOf = propGroup.getOne("is_subscope_of")?.stringValue
        logger.debug { "Resolved scope config (name: $name).".withLocationPrefix(config) }
        return CwtScopeConfigImpl(config, name, aliases, isSubscopeOf)
    }
}

private class CwtScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val aliases: Set<String>,
    override val isSubscopeOf: String?,
) : UserDataHolderBase(), CwtScopeConfig {
    override fun toString() = "CwtScopeConfigImpl(name='$name')"
}
