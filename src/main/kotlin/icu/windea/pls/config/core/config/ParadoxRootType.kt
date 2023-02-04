package icu.windea.pls.config.core.config

enum class ParadoxRootType(
	val id: String,
	val description: String,
	val byRootName: Boolean = false
) {
	Game("game", "Game"),
	Mod("mod", "Mod"),
	
	PdxLauncher("pdx_launcher", "Paradox Launcher", true),
	PdxOnlineAssets("pdx_online_assets", "Paradox Online Assets", true),
	TweakerGuiAssets("tweakergui_assets", "Tweaker GUI Assets", true),
	Jomini("jomini", "Jomini", true);
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
		val valueMapByRootName = buildMap { values().forEach { if(it.byRootName) put(it.id, it) } }
	}
}

