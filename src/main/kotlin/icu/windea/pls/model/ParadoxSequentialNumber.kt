package icu.windea.pls.model

class ParadoxSequentialNumber(
	val name: String,
	val description: String,
	val placeholderText: String
) {
	val popupText = "$name - $description"
	
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