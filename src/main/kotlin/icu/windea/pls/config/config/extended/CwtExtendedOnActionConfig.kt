package icu.windea.pls.config.config.extended

import icu.windea.pls.config.config.*

/**
 * @property name template_expression
 * @property eventType (option) event_type: string
 */
interface CwtExtendedOnActionConfig : CwtExtendedConfig {
    val name: String
    val eventType: String
    
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? {
    val name = when(config) {
        is CwtPropertyConfig -> config.key
        is CwtValueConfig -> config.value
    }
    val eventType = config.findOption("event_type")?.stringValue ?: return null
    return CwtExtendedOnActionConfigImpl(config, name, eventType)
}

private class CwtExtendedOnActionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val eventType: String
) : CwtExtendedOnActionConfig
