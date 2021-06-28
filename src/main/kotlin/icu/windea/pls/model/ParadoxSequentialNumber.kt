package icu.windea.pls.model

import icu.windea.pls.*

class ParadoxSequentialNumber(
	val name: String,
	val description: String,
	val placeholderText: String
) {
	val tailText = " $description"
	val popupText = "$name - $description"
	val icon get() = localisationSequentialNumberIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumber && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}