package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtScopeConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.core.caseInsensitiveStringSet

internal class CwtScopeConfigResolverImpl : CwtScopeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtScopeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeConfig? {
        val name = config.key
        var aliases: Set<String>? = null
        val props = config.properties
        if (props.isNullOrEmpty()) return null
        for (prop in props) {
            if (prop.key == "aliases") aliases = prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }
        if (aliases == null) aliases = emptySet()
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
