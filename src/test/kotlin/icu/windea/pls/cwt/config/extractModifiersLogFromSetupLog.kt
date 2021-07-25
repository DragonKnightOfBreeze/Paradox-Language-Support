package icu.windea.pls.cwt.config

import java.io.*

//解析`modifiers.log`中的modifierDefinitions

//示例：
//...
//[17:28:08][modifier.cpp:885]: Printing Modifier Definitions
//[17:28:08][modifier.cpp:889]: [0] Tag: blank_modifier, Categories: 2

//示例：
//Tag: diplomacy, Categories: character

fun main() {
	extractModifiersLog(
		"src/main/resources/config/stellaris/setup.log", 
		"src/main/resources/config/stellaris/modifiers.ext.log"
	)
}

fun extractModifiersLog(fromPath: String, toPath: String) {
	val fromFile = File(fromPath)
	val toFile = File(toPath)
	val lines = mutableListOf<String>()
	
	var modifierDefinitionStart = false
	var modifierDefinitionEnd = false
	fromFile.forEachLine { line ->
		if(modifierDefinitionStart && !modifierDefinitionEnd){
			val text = getTextByCppFileName(line,"modifier.cpp")
			if(text != null){
				val wsIndex = text.indexOf(' ')
				val t = text.substring(wsIndex + 1)
				lines.add(t)
			}else{
				modifierDefinitionEnd = true
			}
		}
		if(!modifierDefinitionStart && line.endsWith("Printing Modifier Definitions")){
			modifierDefinitionStart = true
		}
	}
	
	toFile.writeText(lines.joinToString("\n"))
}

private fun getTextByCppFileName(line:String,cppFileName:String):String?{
	try{
		val colonIndex = line.indexOf(':',11)
		if(colonIndex == -1) return null
		val actualCppFileName = line.substring(11,colonIndex)
		if(actualCppFileName != cppFileName) return null
		val secondColonIndex = line.indexOf(':',colonIndex+1)
		if(secondColonIndex == -1) return null
		return line.substring(secondColonIndex + 2)
	}catch(e:Exception){
		return null
	}
}