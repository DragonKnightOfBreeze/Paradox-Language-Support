@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.cwt.psi.*

interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @CwtConfig.Option("type: string")
    val type: String

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? = doResolve(config)
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val type = config.findOption("type")?.stringValue ?: return null
    return CwtExtendedDefinitionConfigImpl(config, name, type)
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String
) : UserDataHolderBase(), CwtExtendedDefinitionConfig {
    override fun toString(): String {
        return "CwtExtendedDefinitionConfigImpl(name='$name', type='$type')"
    }
}
