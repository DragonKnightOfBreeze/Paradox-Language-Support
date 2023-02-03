package icu.windea.pls.config.core.config

enum class ParadoxRootType(
	val id: String,
	val description: String
) {
	Game("game", "Game"),
	Mod("mod", "Mod"),
	
	PdxLauncher("pdx_launcher", "Paradox Launcher"),
	PdxOnlineAssets("pdx_online_assets", "Paradox Online Assets"),
	TweakerGuiAssets("tweakergui_assets", "Tweaker GUI Assets"),
	Jomini("jomini", "Jomini");
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
	}
}

