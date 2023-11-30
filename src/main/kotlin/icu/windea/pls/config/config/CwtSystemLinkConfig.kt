package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*

class CwtSystemLinkConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val id: String,
	val baseId: String,
	val description: String,
	val name: String
): CwtConfig<CwtProperty> {
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
}
