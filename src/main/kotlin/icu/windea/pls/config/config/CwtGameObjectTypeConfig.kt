package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.*


/**
 * @property name string
 * @property type (property) type: string
 * @property swapType (property) swap_type: string?
 */
interface CwtGameObjectTypeConfig:CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val type: String
    val swapType: String?
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtGameObjectTypeConfig? = doResolve(config)
    }
}

//Implementations

private fun doResolve(config: CwtPropertyConfig) : CwtGameObjectTypeConfig? {
    val name = config.key
    var type: String? = null
    var swapType: String? = null
    val props = config.properties
    if(props.isNullOrEmpty()) return null
    for(prop in props) {
        when(prop.key) {
            "type" -> type = prop.stringValue
            "swap_type" -> swapType = prop.stringValue
        }
    }
    if(type == null) return null
    return CwtGameObjectTypeConfigImpl(config, name, type, swapType)
}

private class CwtGameObjectTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String,
    override val swapType: String?
): CwtGameObjectTypeConfig