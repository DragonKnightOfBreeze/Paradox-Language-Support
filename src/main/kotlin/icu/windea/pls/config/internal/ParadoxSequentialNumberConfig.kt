package icu.windea.pls.config.internal

import icu.windea.pls.*

class ParadoxSequentialNumberConfig(
	override val id: String,
	override val description: String,
	val placeholderText: String
) : IdAware, DescriptionAware, IconAware {
	val tailText = " $description"
	val popupText = "$id - $description"
	override val icon get() = localisationSequentialNumberIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumberConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return id
	}
}