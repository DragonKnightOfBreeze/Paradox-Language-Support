package icu.windea.pls.model

import icu.windea.pls.*

enum class ParadoxFileType(
	override val key: String,
	override val text: String
):Enumerable{
	ParadoxScript("paradoxScript","Paradox Script File"),
	ParadoxLocalisation("paradoxLocalisation","Paradox Localisation File"),
	Dds("dds","Dds File");
	
	override fun toString(): String {
		return text
	}
}