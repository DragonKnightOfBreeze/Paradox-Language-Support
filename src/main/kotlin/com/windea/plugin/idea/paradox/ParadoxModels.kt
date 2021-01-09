package com.windea.plugin.idea.paradox

import com.intellij.util.ui.*
import java.awt.*
import java.util.*

//File

class ParadoxPath(
	val subPaths:List<String>
){
	val path = subPaths.joinToString("/")
	val fileName = subPaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	val parentSubPaths = subPaths.dropLast(1)
	val parent = parentSubPaths.joinToString("/")
	val root = parentSubPaths.firstOrNull().orEmpty()
	val length = subPaths.size
	
	override fun equals(other: Any?): Boolean {
		return other is ParadoxPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

enum class ParadoxFileType{
	Script,
	Localisation
}

enum class ParadoxGameType(
	val key: String,
	val text: String
) {
	Stellaris("stellaris", "Stellaris")
}

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

data class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootType: ParadoxRootType,
	val gameType: ParadoxGameType
)

//Script

data class ParadoxDefinitionInfo(
	val name:String,
	val type:String,
	val rootKey:String,
	val localisation: Map<ConditionalString,String>,
	val scopes:Map<String,String>,
	val fromVersion:String
){
	val hasLocalisation = localisation.isNotEmpty()
	val hasScopes = scopes.isNotEmpty()
	
	override fun equals(other: Any?): Boolean {
		return other is ParadoxDefinitionInfo && type == other.type && name == other.name
	}
	
	override fun hashCode(): Int {
		return Objects.hash(type, name)
	}
	
	override fun toString(): String {
		return "$name: $type"
	}
}

//Localisation

enum class ParadoxLocale(
	val key: String,
	val description: String
) {
	SIMP_CHINESE("l_simp_chinese", "Simple Chinese"),
	ENGLISH("l_english", "English"),
	BRAZ_POR("l_braz_por", "Brazil Portuguese"),
	FRENCH("l_french", "French"),
	GERMAN("l_german", "German"),
	PONISH("l_polish", "Polish"),
	RUSSIAN("l_russian", "Russian"),
	SPANISH("l_spanish", "Spanish"),
	DEFAULT("l_default", "Default");
	
	val popupText = "'$key' - $description"
	
	companion object {
		val values = values()
		val map = values().associateBy { it.key }
		val keys = values().mapArray { it.key }
	}
}

enum class ParadoxSerialNumber(
	val key: String,
	val description:String,
	val placeholderText :String
) {
	Cardinal("C","Cardinal Number 1, 2, 3...","1"),
	Ordinal("O","Ordinal Number 1st, 2nd, 3rd...","1st"),
	Roman("R","Roman Number I, II, III...","I");
	
	val popupText = "'$key' - $description"
	
	companion object{
		val values = values()
		val map = values().associateBy { it.key }
		val keys = values().mapArray { it.key }
	}
}

enum class ParadoxColor(
	val key: String,
	val description: String,
	val color: Color,
	val colorText:String
) {
	Blue("B", "Blue", Color(0x0000ff),"#0000ff"),
	Teal("E", "Teal", Color(0x008080),"#008080"),
	Green("G", "Green", Color(0x00ff00),"#00ff00"),
	Orange("H", "Orange", Color(0xffa500),"#ffa500"),
	Brown("L", "Brown", Color(0xa52a2a),"#a52a2a"),
	Purple("M", "Purple", Color(0x800080),"#800080"),
	LightRed("P", "Light Red", Color(0xcd5c5c),"#cd5c5c"),
	Red("R", "Red", Color(0xff0000),"#ff0000"),
	DarkOrange("S", "Dark Orange", Color(0xff8c00),"#ff8c00"),
	LightGrey("T", "Light Grey", Color(0xd3d3d3),"#d3d3d3"),
	White("W", "White", Color(0xffffff),"#ffffff"),
	Yellow("Y", "Yellow", Color(0xffff00),"#ffff00");
	
	val popupText = "'$key' - $description"
	val icon = ColorIcon(16, color)
	val gutterIcon = ColorIcon(12, color)
	
	companion object {
		val values = values()
		val map = values().associateBy { it.key }
		val keys = values().mapArray { it.key }
	}
}


