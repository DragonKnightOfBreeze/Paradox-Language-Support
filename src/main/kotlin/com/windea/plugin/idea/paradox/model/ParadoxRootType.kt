package com.windea.plugin.idea.paradox.model

enum class ParadoxRootType(
	val key:String,
	val text: String
) {
	Stdlib("stdlib","Stdlib"),
	Mod("mod","Mod"),
	PdxLauncher("pdx_launcher","Paradox Launcher"),
	PdxOnlineAssets("pdx_online_assets","Paradox Online Assets"),
	TweakerGuiAssets("tweakergui_assets","Tweaker GUI Assets")
}