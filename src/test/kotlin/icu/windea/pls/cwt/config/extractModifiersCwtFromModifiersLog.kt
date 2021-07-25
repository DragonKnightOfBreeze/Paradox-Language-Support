package icu.windea.pls.cwt.config

import java.io.*

//从modifiers.log生成modifiers.cwt

//示例：
//Printing Modifier Definitions:
//Tag: diplomacy, Categories: character

//示例：
//modifiers = {
//    diplomacy = character
//}

fun main() {
	extractModifiersCwt(
		"src/main/resources/config/stellaris/modifiers.log",
		"src/main/resources/config/stellaris/modifiers.ext.cwt" 
	)
	extractModifiersCwt(
		"src/main/resources/config/ck3/script-docs/modifiers.log",
		"src/main/resources/config/ck3/modifiers.ext.cwt"
	)
}

fun extractModifiersCwt(fromPath: String, toPath: String) {
	val fromFile = File(fromPath)
	val toFile = File(toPath)
	val configs = mutableListOf<Pair<String,String>>()
	
	fromFile.inputStream().bufferedReader().forEachLine { line ->
		val colonIndex = line.indexOf(':')
		if(colonIndex == -1) return@forEachLine
		val commaIndex = line.indexOf(',', colonIndex + 2)
		if(commaIndex == -1) return@forEachLine
		val colonIndex2 = line.indexOf(':', commaIndex + 2)
		if(colonIndex2 == -1) return@forEachLine
		val tag = line.substring(colonIndex + 2, commaIndex)
		val categories = line.substring(colonIndex2 + 2)
		configs.add(tag to categories)
	}
	toFile.writeText(configs.joinToString("\n","modifiers = {\n","\n}"){ (tag,categories)-> "    $tag = $categories" })
}
	