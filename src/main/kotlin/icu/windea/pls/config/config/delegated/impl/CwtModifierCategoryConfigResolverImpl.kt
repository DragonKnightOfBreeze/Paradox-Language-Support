package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtModifierCategoryConfigResolverImpl : CwtModifierCategoryConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid modifier category config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        // may be empty here (e.g., "AI Economy")
        val supportedScopes = propElements.find { it.key == "supported_scopes" }?.let { prop ->
            buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
        }?.optimized() ?: ParadoxScopeManager.anyScopeIdSet
        logger.debug { "Resolved modifier category config (name: $name).".withLocationPrefix(config) }
        return CwtModifierCategoryConfigImpl(config, name, supportedScopes)
    }
}

private class CwtModifierCategoryConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtModifierCategoryConfig {
    override fun toString() = "CwtModifierCategoryConfigImpl(name='$name')"
}
