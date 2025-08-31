@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * @property name string
 * @property supportedScopes (value) string[]
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig
    }

    companion object : Resolver by CwtLocalisationCommandConfigResolverImpl()
}
