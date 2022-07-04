package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

enum class ParadoxGameType(
	override val id: String,
	override val description: String,
	val gameSteamId: String
) : IdAware, DescriptionAware {
	Ck2("ck2", "Crusader Kings II", "203770"),
	Ck3("ck3", "Crusader Kings III", "1158310"),
	Eu4("eu4", "Europa Universalis IV", "236850"),
	Hoi4("hoi4", "Hearts of Iron IV", "394360"),
	Ir("ir", "Imperator: Rome", "859580"),
	Stellaris("stellaris", "Stellaris", "281990"),
	Vic2("vic2", "Victoria II", "42960");
	
	val gameName: String get() = description
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
		val valueList = values.toList()
		val valueMap = values.associateBy { it.id }
		
		fun resolve(id: String): ParadoxGameType? {
			return valueMap[id.lowercase()]
		}
		
		fun resolve(markerFile: VirtualFile): ParadoxGameType? {
			try {
				if(markerFile.isDirectory) return null //非目录
				val markerFileName = markerFile.name
				return when {
					markerFileName == launcherSettingsFileName -> {
						val gameId = jsonMapper.readTree(markerFile.inputStream).get("gameId").textValue()
						resolve(gameId)
					}
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

fun ParadoxGameType?.orDefault() = this ?: getSettings().defaultGameType