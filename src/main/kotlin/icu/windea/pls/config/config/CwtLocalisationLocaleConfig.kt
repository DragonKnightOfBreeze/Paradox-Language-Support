package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*

//EXTENDED BY PLS

class CwtLocalisationLocaleConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val id: String,
	val description: String,
	val codes: List<String>
) : CwtConfig<CwtProperty> {
	val icon get() = PlsIcons.LocalisationLocale
	
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
}
