package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtLocalisationCommandConfigResolverImpl : CwtLocalisationCommandConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig {
        val name = config.key
        val supportedScopes = buildSet {
            config.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
            config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
        }.ifEmpty { ParadoxScopeManager.anyScopeIdSet }
        return CwtLocalisationCommandConfigImpl(config, name, supportedScopes)
    }
}

private class CwtLocalisationCommandConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationCommandConfig {
    override fun toString() = "CwtLocalisationCommandConfigImpl(name='$name')"
}
