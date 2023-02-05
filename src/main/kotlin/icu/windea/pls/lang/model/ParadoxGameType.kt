package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

/**
 * @param entries 对于此游戏类型，匹配CWT规则时，也需要基于哪些子目录。
 */
enum class ParadoxGameType(
	val id: String,
	val description: String,
	val gameSteamId: String,
	val entries: List<String> = emptyList(),
) {
	Ck2("ck2",
		"Crusader Kings II",
		"203770",
		listOf("game", "jomini")
	),
	Ck3("ck3",
		"Crusader Kings III",
		"1158310",
		listOf("game", "jomini")
	),
	Eu4("eu4",
		"Europa Universalis IV",
		"236850"
	),
	Hoi4("hoi4",
		"Hearts of Iron IV",
		"394360"
	),
	Ir("ir",
		"Imperator: Rome",
		"859580"
	),
	Stellaris("stellaris",
		"Stellaris",
		"281990",
		listOf("pdx_launcher/game", "pdx_launcher/common", "tweakergui_assets")
	),
	Vic2("vic2",
		"Victoria II",
		"42960"
	),
	Vic3("vic3",
		"Victoria III",
		"529340"
	);
	
	val gameName: String get() = description
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
		val valueList = values.toList()
		val valueMap = values.associateBy { it.id }
		
		fun resolve(id: String): ParadoxGameType? {
			if(id.isEmpty()) return null
			return valueMap[id.lowercase()]
		}
		
		fun resolve(markerFile: VirtualFile): ParadoxGameType? {
			// launcher-settings.json / descriptor.mod 
			try {
				if(markerFile.isDirectory) return null //非目录
				val markerFileName = markerFile.name
				return when {
					markerFileName.startsWith('.') -> {
						val gameId = markerFileName.drop(1)
						resolve(gameId)
					}
					else -> {
						null
					}
				}
			} catch(e: Exception) {
				return null
			}
		}
	}
}

val ParadoxGameType?.id get() = this?.id ?: "core"

fun ParadoxGameType?.orDefault() = this ?: getSettings().defaultGameType