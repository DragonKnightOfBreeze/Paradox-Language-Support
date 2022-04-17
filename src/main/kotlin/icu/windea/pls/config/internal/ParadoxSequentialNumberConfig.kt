package icu.windea.pls.config.internal

import icu.windea.pls.*

class ParadoxSequentialNumberConfig(
	val name: String,
	val description: String,
	val placeholderText: String
) {
	val tailText = " $description"
	val popupText = "$name - $description"
	val icon get() = localisationSequentialNumberIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumberConfig && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}