@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.config.util.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    @Option("hint: string?")
    val hint: String?
    val configForDeclaration: CwtPropertyConfig?

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig = doResolve(config)
    }
}

//Implementations (not interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedGameRuleConfigImpl(config, name, hint)
}

private class CwtExtendedGameRuleConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedGameRuleConfig {
    override val configForDeclaration: CwtPropertyConfig? by lazy {
        if (config !is CwtPropertyConfig) return@lazy null
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    override fun toString(): String {
        return "CwtExtendedGameRuleConfigImpl(name='$name')"
    }
}

