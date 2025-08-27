@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxScopeManager

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

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig {
    val name = config.key
    val supportedScopes = buildSet {
        config.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
        config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
    }.ifEmpty { ParadoxScopeManager.anyScopeIdSet }
    return CwtLocalisationCommandConfigImpl(config, name, supportedScopes)
}

private class CwtLocalisationCommandConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationCommandConfig {
    override fun toString(): String {
        return "CwtLocalisationCommandConfigImpl(name='$name')"
    }
}
