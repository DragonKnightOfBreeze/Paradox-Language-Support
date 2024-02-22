package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

/**
 * @property name template_expression
 * @property eventType (option) event_type: string
 */
interface CwtOnActionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    val name: String
    val eventType: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtOnActionConfig? = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtMemberConfig<*>): CwtOnActionConfig? {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val eventType = config.findOption("event_type")?.stringValue ?: return null
    return CwtOnActionConfigImpl(config, name, eventType)
}

private class CwtOnActionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val eventType: String
) : CwtOnActionConfig
