package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtSystemScopeConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue

internal class CwtSystemScopeConfigResolverImpl : CwtSystemScopeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSystemScopeConfig {
        val id = config.key
        val baseId = config.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
        val name = config.stringValue ?: id
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
