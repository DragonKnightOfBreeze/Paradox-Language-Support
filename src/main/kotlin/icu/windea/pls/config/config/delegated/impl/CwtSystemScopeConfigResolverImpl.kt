package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix

internal class CwtSystemScopeConfigResolverImpl : CwtSystemScopeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSystemScopeConfig {
        val id = config.key
        val baseId = config.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
        val name = config.stringValue ?: id
        logger.debug { "Resolved system scope config (id: $id).".withLocationPrefix(config) }
        return CwtSystemScopeConfigImpl(config, id, baseId, name)
    }
}

private class CwtSystemScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val baseId: String,
    override val name: String
) : UserDataHolderBase(), CwtSystemScopeConfig {
    override fun equals(other: Any?) = this === other || other is CwtSystemScopeConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "CwtSystemScopeConfigImpl(name='$name')"
}
