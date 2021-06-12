package icu.windea.pls.model

import icu.windea.pls.*
import javax.swing.*

enum class ParadoxGameType(
	val key: String,
	val text: String,
	val icon: Icon
) {
	Ck2("ck2","Crusader Kings II", ck2Icon),
	Ck3("ck3","Crusader Kings III", ck3Icon),
	Eu4("eu4","Europa Universalis IV", eu4Icon),
	Hoi4("hoi4","Hearts of Iron IV", hoi4Icon),
	Ir("ir","Imperator: Rome", irIcon),
	Stellaris("stellaris", "Stellaris", stellarisIcon),
	Vic2("vic2","Victoria II", vic2Icon);
	
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
			return getSettings().defaultGameType
		}
	}
}