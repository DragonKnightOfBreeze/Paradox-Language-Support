package icu.windea.pls.model

import java.util.*

data class ParadoxLocalisationInfo(
	val name:String,
	val category: ParadoxLocalisationCategory,
){
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocalisationInfo && name == other.name && category == other.category
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, category)
	}
}