package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import javax.swing.*

enum class ParadoxGameType(
	override val id: String,
	override val description: String,
	override val icon: Icon
) : IdAware, DescriptionAware, IconAware {
	Ck2("ck2", "Crusader Kings II", PlsIcons.libraryIcon),
	Ck3("ck3", "Crusader Kings III", PlsIcons.libraryIcon),
	Eu4("eu4", "Europa Universalis IV", PlsIcons.libraryIcon),
	Hoi4("hoi4", "Hearts of Iron IV", PlsIcons.libraryIcon),
	Ir("ir", "Imperator: Rome", PlsIcons.libraryIcon),
	Stellaris("stellaris", "Stellaris", PlsIcons.libraryIcon),
	Vic2("vic2", "Victoria II", PlsIcons.libraryIcon);
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		val values = values()
		val map = values.associateBy { it.id }
		
		fun resolve(id: String): ParadoxGameType? {
			return map[id.lowercase()]
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