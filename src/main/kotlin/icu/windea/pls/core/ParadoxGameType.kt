package icu.windea.pls.core

import icu.windea.pls.*
import javax.swing.*

enum class ParadoxGameType(
	override val id: String,
	override val description: String,
	override val icon: Icon
) : IdAware, DescriptionAware, IconAware {
	//NOTE 暂时使用统一的库的图标
	Ck2("ck2", "Crusader Kings II", PlsIcons.libraryIcon),
	Ck3("ck3", "Crusader Kings III", PlsIcons.libraryIcon),
	Eu4("eu4", "Europa Universalis IV", PlsIcons.libraryIcon),
	Hoi4("hoi4", "Hearts of Iron IV", PlsIcons.libraryIcon),
	Ir("ir", "Imperator: Rome", PlsIcons.libraryIcon),
	Stellaris("stellaris", "Stellaris", PlsIcons.libraryIcon),
	Vic2("vic2", "Victoria II", PlsIcons.libraryIcon);
	
	//NOTE 明确的执行文件名称，应当就是这样
	val exeFileName = "$id.exe"
	
	override fun toString(): String {
		return id
	}
	
	companion object {
		val values = values()
		val map = values.associateBy { it.id }
		val exeFileNames = values.map { it.exeFileName }
		
		fun resolve(id: String): ParadoxGameType? {
			return map[id.lowercase()]
		}
		
		fun isValidKey(id: String): Boolean {
			return id.lowercase() in map
		}
		
		fun defaultValue(): ParadoxGameType {
			return getSettings().defaultGameType
		}
	}
}