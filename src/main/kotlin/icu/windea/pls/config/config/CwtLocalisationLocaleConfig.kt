package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*

class CwtLocalisationLocaleConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val id: String,
    val description: String,
    val codes: List<String>
) : CwtConfig<CwtProperty> {
    val icon get() = PlsIcons.LocalisationNodes.Locale
    
    val text = buildString {
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
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationLocaleConfig {
            val id = config.key
            val description = config.documentation.orEmpty()
            val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
            return CwtLocalisationLocaleConfig(config.pointer, config.info, id, description, codes)
        }
    }
}
