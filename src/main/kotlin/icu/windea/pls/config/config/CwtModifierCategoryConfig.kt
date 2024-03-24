package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*

/**
 * @property name string
 * @property supportedScopes supported_scopes: string | string[]
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val supportedScopes: Set<String>
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtModifierCategoryConfigImpl? {
    val name = config.key
    var supportedScopes: Set<String>? = null
    val props = config.properties
    if(props.isNullOrEmpty()) return null
    for(prop in props) {
        when(prop.key) {
            "supported_scopes" -> supportedScopes = buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
            } //may be empty here (e.g. "AI Economy")
        }
    }
    supportedScopes = supportedScopes ?: ParadoxScopeHandler.anyScopeIdSet
    return CwtModifierCategoryConfigImpl(config, name, supportedScopes)
}

private class CwtModifierCategoryConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : CwtModifierCategoryConfig
