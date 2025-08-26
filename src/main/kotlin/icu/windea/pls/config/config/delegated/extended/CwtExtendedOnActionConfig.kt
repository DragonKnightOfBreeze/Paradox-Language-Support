@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.cwt.psi.*

interface CwtExtendedOnActionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @Option("event_type: string")
    val eventType: String
    @Option("hint: string?")
    val hint: String?

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? = doResolve(config)
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val eventType = config.findOption("event_type")?.stringValue ?: return null
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedOnActionConfigImpl(config, name, eventType, hint)
}

private class CwtExtendedOnActionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val eventType: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedOnActionConfig {
    override fun toString(): String {
        return "CwtExtendedOnActionConfigImpl(name='$name', eventType='$eventType')"
    }
}
