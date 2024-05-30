package icu.windea.pls.config.config

import icons.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

interface CwtLocalisationPredefinedParameterConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val id: String
    val mockValue: String
    val description: String
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPredefinedParameterConfig {
            return doResolve(config)
        }
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationPredefinedParameterConfig {
    val id = config.key
    val mockValue = config.value
    val description = config.documentation.orEmpty()
    return CwtLocalisationPredefinedParameterConfigImpl(config, id, mockValue, description)
}

private class CwtLocalisationPredefinedParameterConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val mockValue: String,
    override val description: String
) : CwtLocalisationPredefinedParameterConfig {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationPredefinedParameterConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
}