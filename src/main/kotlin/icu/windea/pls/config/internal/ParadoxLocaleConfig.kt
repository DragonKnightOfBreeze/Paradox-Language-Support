package icu.windea.pls.config.internal

import icu.windea.pls.*

class ParadoxLocaleConfig(
	val name: String,
	val description: String
) {
	val tailText = " $description"
	val popupText = "$name - $description"
	val icon get() = localisationLocaleIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocaleConfig && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}