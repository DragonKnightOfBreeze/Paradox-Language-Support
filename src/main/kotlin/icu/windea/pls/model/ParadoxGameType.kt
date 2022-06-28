package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

enum class ParadoxGameType(
	override val id: String,
	override val    description: String
) : IdAware, DescriptionAware {
	Ck2("ck2", "Crusader Kings II"),
	Ck3("ck3", "Crusader Kings III"),
	Eu4("eu4", "Europa Universalis IV"),
	Hoi4("hoi4", "Hearts of Iron IV"),
	Ir("ir", "Imperator: Rome"),
	Stellaris("stellaris", "Stellaris"),
	Vic2("vic2", "Victoria II");
	
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