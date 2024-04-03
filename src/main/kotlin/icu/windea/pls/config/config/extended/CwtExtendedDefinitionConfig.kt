package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*

/**
 * @property name template_expression
 * @property type (option) type: string
 */
interface CwtExtendedDefinitionConfig : CwtExtendedConfig {
    val name: String
    val type: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val type = config.findOption("type")?.stringValue ?: return null
    return CwtExtendedDefinitionConfigImpl(config, name, type)
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String
) : CwtExtendedDefinitionConfig 
