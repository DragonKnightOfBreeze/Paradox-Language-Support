@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    @FromProperty("aliases: string[]")
    val aliases: Set<@CaseInsensitive String>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtScopeConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

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

private class CwtScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val aliases: Set<String>
) : UserDataHolderBase(), CwtScopeConfig {
    override fun toString(): String {
        return "CwtScopeConfigImpl(name='$name')"
    }
}
