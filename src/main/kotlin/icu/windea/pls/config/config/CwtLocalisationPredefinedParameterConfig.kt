package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

//EXTENDED BY PLS

class CwtLocalisationPredefinedParameterConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val id: String,
	val mockValue: String,
	val description: String
) : CwtConfig<CwtProperty> {
	val icon: Icon get() = PlsIcons.PredefinedParameter
	
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
