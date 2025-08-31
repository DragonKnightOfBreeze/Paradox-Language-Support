@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.impl.CwtDeclarationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * @property name string
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String

    val configForDeclaration: CwtPropertyConfig
    val subtypesUsedInDeclaration: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig?
    }

    companion object : Resolver by CwtDeclarationConfigResolverImpl()
}
