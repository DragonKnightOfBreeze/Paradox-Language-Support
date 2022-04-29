package icu.windea.pls.config.internal.config

import icu.windea.pls.*

class ParadoxLocaleConfig(
	override val id: String,
	override val description: String
) : IdAware, DescriptionAware, IconAware {
	val tailText = " $description"
	val popupText = "$id - $description"
	override val icon get() = PlsIcons.localisationLocaleIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocaleConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return id
	}
}