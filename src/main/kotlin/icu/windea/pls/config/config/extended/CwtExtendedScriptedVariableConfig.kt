package icu.windea.pls.config.config.extended


import icu.windea.pls.config.config.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property hint (option) hint: string?
 */
interface CwtExtendedScriptedVariableConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val hint: String?
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig? {
    val name = when(config) {
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
) : CwtExtendedScriptedVariableConfig 
