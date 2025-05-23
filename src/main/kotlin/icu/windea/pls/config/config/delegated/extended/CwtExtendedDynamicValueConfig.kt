@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property type string
 * @property hint (option) hint: string?
 */
interface CwtExtendedDynamicValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val type: String
    val hint: String?

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig = doResolve(config, type)
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedDynamicValueConfigImpl(config, name, type, hint)
}

private class CwtExtendedDynamicValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDynamicValueConfig {
    override fun toString(): String {
        return "CwtExtendedDynamicValueConfigImpl(name='$name', type='$type')"
    }
}
