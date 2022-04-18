package icu.windea.pls.core

import icu.windea.pls.*

enum class ParadoxFileType(
	override val id: String,
	override val description: String
) : IdAware, DescriptionAware {
	ParadoxScript("paradoxScript", "Paradox Script File"),
	ParadoxLocalisation("paradoxLocalisation", "Paradox Localisation File"),
	Dds("dds", "Dds File");
	
	override fun toString(): String {
		return description
	}
}