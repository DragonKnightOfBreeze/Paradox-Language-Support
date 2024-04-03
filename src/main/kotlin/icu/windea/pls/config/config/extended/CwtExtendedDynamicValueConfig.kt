package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*

/**
 * @property name template_expression
 * @property type string
 * @property hint (option) hint: string?
 */
interface CwtExtendedDynamicValueConfig : CwtExtendedConfig {
    val name: String
    val type: String
    val hint: String?
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig = doResolve(config, type)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig {
    val name = when(config) {
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
) : CwtExtendedDynamicValueConfig