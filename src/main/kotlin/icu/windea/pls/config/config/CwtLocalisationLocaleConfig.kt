package icu.windea.pls.config.config

import icons.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

interface CwtLocalisationLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val id: String
    val description: String
    val codes: List<String>
    val text: String
    
    val shortId: String get() = id.removePrefix("l_")
    
    companion object {
        val AUTO: CwtLocalisationLocaleConfig = AutoCwtLocalisationLocaleConfig
        
        fun resolve(config: CwtPropertyConfig): CwtLocalisationLocaleConfig = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationLocaleConfig {
    val id = config.key
    val description = config.documentation.orEmpty()
    val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
    return CwtLocalisationLocaleConfigImpl(config, id, description, codes)
}

private class CwtLocalisationLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val description: String,
    override val codes: List<String>
) : CwtLocalisationLocaleConfig {
    override val text = buildString {
        append(id)
        if(description.isNotEmpty()) append(" (").append(description).append(")")
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocaleConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
}

private object AutoCwtLocalisationLocaleConfig: CwtLocalisationLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val id: String = "auto"
    override val description: String get() = PlsBundle.message("locale.auto")
    override val codes: List<String> get() = emptyList()
    override val text: String get() = description
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocaleConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
}