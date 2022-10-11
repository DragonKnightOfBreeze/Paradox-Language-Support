package icu.windea.pls.core.model

enum class ParadoxRootType(
	val id: String,
	val description: String
) {
	Game("game", "Game"),
	Mod("mod", "Mod"),
	PdxLauncher("pdx_launcher", "Paradox Launcher"),
	PdxOnlineAssets("pdx_online_assets", "Paradox Online Assets"),
	TweakerGuiAssets("tweakergui_assets", "Tweaker GUI Assets");
	
	override fun toString(): String {
		return description
	}
}

