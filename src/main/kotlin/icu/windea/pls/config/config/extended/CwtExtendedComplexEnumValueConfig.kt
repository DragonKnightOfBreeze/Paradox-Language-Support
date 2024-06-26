package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*

/**
 * @property name template_expression
 * @property type string
 * @property hint (option) hint: string?
 */
interface CwtExtendedComplexEnumValueConfig : CwtExtendedConfig {
    val name: String
    val type: String
    val hint: String?
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig = doResolve(config, type)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val hint = config.findOption("hint")?.stringValue
    return CwtExtendedComplexEnumValueConfigImpl(config, name, type, hint)
}

private class CwtExtendedComplexEnumValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : CwtExtendedComplexEnumValueConfig 