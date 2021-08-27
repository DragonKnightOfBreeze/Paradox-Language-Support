package icu.windea.pls.model

import icu.windea.pls.*
import javax.swing.*

enum class ParadoxGameType(
	override val key: String,
	override val text: String,
	val icon: Icon
):Enumerable {
	//NOTE 暂时使用统一的库的图标
	Ck2("ck2","Crusader Kings II", libraryIcon),
	Ck3("ck3","Crusader Kings III", libraryIcon),
	Eu4("eu4","Europa Universalis IV", libraryIcon),
	Hoi4("hoi4","Hearts of Iron IV", libraryIcon),
	Ir("ir","Imperator: Rome", libraryIcon),
	Stellaris("stellaris", "Stellaris", libraryIcon),
	Vic2("vic2","Victoria II", libraryIcon);
	
	//NOTE 明确的执行文件名称，应当就是这样
	val exeFileName = "$key.exe"
	
	override fun toString(): String {
		return text
	}
	
	companion object{
		val values = values() 
		val map = values.associateBy { it.key }
		val exeFileNames = values.map { it.exeFileName }
		
		fun resolve(key:String): ParadoxGameType? {
			return map[key.lowercase()] 
		}
		
		fun isValidKey(key:String):Boolean {
			return key.lowercase() in map
		}
		
		fun defaultValue(): ParadoxGameType{
			return defaultGameType
		}
	}
}