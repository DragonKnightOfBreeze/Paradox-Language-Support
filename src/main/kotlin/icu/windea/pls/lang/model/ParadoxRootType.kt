package icu.windea.pls.lang.model

enum class ParadoxRootType(
	val id: String,
	val description: String,
) {
	Game("game", "Game"),
	Mod("mod", "Mod");
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
	}
}

