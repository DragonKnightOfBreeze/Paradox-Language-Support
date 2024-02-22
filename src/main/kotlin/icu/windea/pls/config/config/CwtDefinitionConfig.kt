package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property type (option) type: string
 */
interface CwtDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val type: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtDefinitionConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtMemberConfig<*>): CwtDefinitionConfig? {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val type = config.findOption("type")?.stringValue ?: return null
    return CwtDefinitionConfigImpl(config, name, type)
}

private class CwtDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String
) : CwtDefinitionConfig 
