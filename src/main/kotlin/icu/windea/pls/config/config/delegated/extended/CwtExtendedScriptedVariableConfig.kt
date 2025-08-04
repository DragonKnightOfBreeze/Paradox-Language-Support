@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.cwt.psi.*

interface CwtExtendedScriptedVariableConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @Option("hint: string?")
    val hint: String?

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig? = doResolve(config)
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedScriptedVariableConfigImpl(config, name, hint)
}

private class CwtExtendedScriptedVariableConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedScriptedVariableConfig {
    override fun toString(): String {
        return "CwtExtendedScriptedVariableConfigImpl(name='$name')"
    }
}
