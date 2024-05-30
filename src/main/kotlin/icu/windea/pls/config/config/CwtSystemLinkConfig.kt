package icu.windea.pls.config.config

import icons.*
import icu.windea.pls.cwt.psi.*

interface CwtSystemLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val id: String
    val baseId: String
    val description: String
    val name: String
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtSystemLinkConfig = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtSystemLinkConfig {
    val id = config.key
    val baseId = config.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
    val description = config.documentation.orEmpty()
    val name = config.stringValue ?: id
    return CwtSystemLinkConfigImpl(config, id, baseId, description, name)
}

private class CwtSystemLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val baseId: String,
    override val description: String,
    override val name: String
) : CwtSystemLinkConfig {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSystemLinkConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
}