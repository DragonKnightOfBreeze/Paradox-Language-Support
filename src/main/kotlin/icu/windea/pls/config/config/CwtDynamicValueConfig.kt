package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property type string
 * @property hint (option) hint: string?
 */
interface CwtDynamicValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val type: String
    val hint: String?
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtDynamicValueConfig = doResolve(config, type)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtDynamicValueConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val hint = config.findOption("hint")?.stringValue
    return CwtDynamicValueConfigImpl(config, name, type, hint)
}

private class CwtDynamicValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : CwtDynamicValueConfig