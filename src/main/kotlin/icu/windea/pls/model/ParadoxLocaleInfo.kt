package icu.windea.pls.model

import icu.windea.pls.*

class ParadoxLocaleInfo(
	val name: String,
	val description: String
) {
	val tailText = " $description"
	val popupText = "$name - $description"
	val icon get() = localisationLocaleIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocaleInfo && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}