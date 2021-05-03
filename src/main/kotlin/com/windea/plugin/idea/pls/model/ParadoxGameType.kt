package com.windea.plugin.idea.pls.model

import com.windea.plugin.idea.pls.*

enum class ParadoxGameType(
	val key: String,
	val text: String
) {
	Ck2("ck2","Crusader Kings II"),
	Ck3("ck3","Crusader Kings III"),
	Eu4("eu4","Europa Universalis IV"),
	Hoi4("hoi4","Hearts of Iron IV"),
	Ir("ir","Imperator: Rome"),
	Stellaris("stellaris", "Stellaris"),
	Vic2("vic2","Victoria II");
	
	companion object{
		val values = values() 
		val map = values.associateBy { it.key }
		
		fun resolve(key:String): ParadoxGameType? {
			return map[key.toLowerCase()] 
		}
		
		fun isValidKey(key:String):Boolean{
			return key.toLowerCase() in map
		}
		
		fun defaultValue(): ParadoxGameType{
			return resolve(settings.defaultGameType)?: Stellaris
		}
	}
}