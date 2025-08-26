@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.cwt.psi.*

interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @Option("type: string")
    val type: String
    @Option("hint: string?")
    val hint: String?

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
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedDefinitionConfigImpl(config, name, type, hint)
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDefinitionConfig {
    override fun toString(): String {
        return "CwtExtendedDefinitionConfigImpl(name='$name', type='$type')"
    }
}
