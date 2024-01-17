package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*

class CwtSystemLinkConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val id: String,
    val baseId: String,
    val description: String,
    val name: String
) : CwtConfig<CwtProperty> {
    val icon get() = PlsIcons.Nodes.SystemScope
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSystemLinkConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtSystemLinkConfig {
            val id = config.key
            val baseId = config.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
            val description = config.documentation.orEmpty()
            val name = config.stringValue ?: id
            return CwtSystemLinkConfig(config.pointer, config.info, id, baseId, description, name)
        }
    }
}
