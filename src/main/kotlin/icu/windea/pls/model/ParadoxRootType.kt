package icu.windea.pls.model

import icu.windea.pls.*

enum class ParadoxRootType(
	override val id: String,
	override val description: String
) : IdAware, DescriptionAware {
	Game("game", "Game"),
	Mod("mod", "Mod"),
	PdxLauncher("pdx_launcher", "Paradox Launcher"),
	PdxOnlineAssets("pdx_online_assets", "Paradox Online Assets"),
	TweakerGuiAssets("tweakergui_assets", "Tweaker GUI Assets");
	
	override fun toString(): String {
		return description
	}
}

