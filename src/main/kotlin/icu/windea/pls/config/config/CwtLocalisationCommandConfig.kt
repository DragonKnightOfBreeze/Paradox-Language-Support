package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property name string
 * @property supportedScopes (value) string[]
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val supportedScopes: Set<String>
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationCommandConfigImpl {
    val name = config.key
    val supportedScopes = buildSet {
        config.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
        config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
    }.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
    return CwtLocalisationCommandConfigImpl(config, name, supportedScopes)
}

private class CwtLocalisationCommandConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : CwtLocalisationCommandConfig