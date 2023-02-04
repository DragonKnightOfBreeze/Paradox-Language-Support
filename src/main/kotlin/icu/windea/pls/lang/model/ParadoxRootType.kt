package icu.windea.pls.lang.model

enum class ParadoxRootType(
	val id: String,
	val description: String,
	val byRootName: Boolean = false,
) {
	Game("game", "Game"),
	Mod("mod", "Mod"),
	
	//px_launcher/game pdx_launcher/common for Stellaris
	PdxLauncher("pdx_launcher", "Pdx Launcher", true),
	//pdx_online_assets
	PdxOnlineAssets("pdx_online_assets", "Pdx Online Assets", true),
	//tweakergui_assets
	TweakerGuiAssets("tweakergui_assets", "Tweaker GUI Assets", true),
	//jomini
	Jomini("jomini", "Jomini", true);
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
		val valueMapByRootName = buildMap { values().forEach { if(it.byRootName) put(it.id, it) } }
	}
}

