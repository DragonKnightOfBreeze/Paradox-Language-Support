package icu.windea.pls.config.config

import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name string
 * @property desc desc: string?
 * @property inputScopes input_scopes: string[]
 * @property outputScope output_scope: string?
 */
interface CwtLocalisationLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val desc: String?
    val inputScopes: Set<String>
    val outputScope: String?
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationLinkConfig? = doResolve(config)
        
        fun resolveFromLink(config: CwtLinkConfig): CwtLocalisationLinkConfig = doResolveFromLink(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationLinkConfig? {
    val name = config.key
    var desc: String? = null
    var inputScopes: Set<String>? = null
    var outputScope: String? = null
    val props = config.properties ?: return null
    for(prop in props) {
        when(prop.key) {
            "desc" -> desc = prop.stringValue?.trim() //排除占位码 & 去除首尾空白
            "input_scopes" -> inputScopes = buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
            }
            "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
        }
    }
    inputScopes = inputScopes.orNull() ?: ParadoxScopeHandler.anyScopeIdSet
    return CwtLocalisationLinkConfigImpl(config, name, desc, inputScopes, outputScope)
}

private fun doResolveFromLink(config: CwtLinkConfig): CwtLocalisationLinkConfig {
    return CwtLocalisationLinkConfigImpl(config.config, config.name, config.desc, config.inputScopes, config.outputScope)
}

private class CwtLocalisationLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val desc: String? = null,
    override val inputScopes: Set<String>,
    override val outputScope: String?
) : CwtLocalisationLinkConfig
