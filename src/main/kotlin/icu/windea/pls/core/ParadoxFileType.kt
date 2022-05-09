package icu.windea.pls.core

import icu.windea.pls.*

enum class ParadoxFileType(
	override val id: String,
	override val description: String
) : IdAware, DescriptionAware {
	Directory("directory", "Directory"),
	ParadoxScript("paradoxScript", "Paradox Script File"),
	ParadoxLocalisation("paradoxLocalisation", "Paradox Localisation File"),
	Dds("dds", "Dds File"),
	Other("other", "Other File");
	
	override fun toString(): String {
		return id
	}
}