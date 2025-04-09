@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name string
 * @property supportedScopes (property) supported_scopes: string | string[]
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val supportedScopes: Set<String>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? {
    val name = config.key
    var supportedScopes: Set<String>? = null
    val props = config.properties
    if (props.isNullOrEmpty()) return null
    for (prop in props) {
        when (prop.key) {
            //may be empty here (e.g., "AI Economy")
            "supported_scopes" -> supportedScopes = buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
        }
    }
    supportedScopes = supportedScopes?.optimized() ?: ParadoxScopeManager.anyScopeIdSet
    return CwtModifierCategoryConfigImpl(config, name, supportedScopes)
}

private class CwtModifierCategoryConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtModifierCategoryConfig
