package icu.windea.pls.config.config.extended

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val configForDeclaration: CwtPropertyConfig?

    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig {
    val name = when (config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    return CwtExtendedGameRuleConfigImpl(config, name)
}

private class CwtExtendedGameRuleConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String
) : UserDataHolderBase(), CwtExtendedGameRuleConfig {
    override val configForDeclaration: CwtPropertyConfig? by lazy {
        if (config !is CwtPropertyConfig) return@lazy null
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }
}

