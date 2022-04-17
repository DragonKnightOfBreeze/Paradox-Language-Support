package icu.windea.pls.config.cwt

import java.io.*

//从modifiers.log生成modifiers.cwt

//输入示例：
//Printing Modifier Definitions:
//- diplomacy, Categories: character

//输出示例：
//modifiers = {
//    diplomacy = character
//}

fun main() {
	extractModifiersCwt(
		"src/main/resources/config/cwt/stellaris/logs/modifiers.log",
		"src/main/resources/config/cwt/stellaris/modifiers.pls.cwt" 
	)
}

fun extractModifiersCwt(fromPath: String, toPath: String) {
	val fromFile = File(fromPath)
	val toFile = File(toPath)
	val regex = """- (.*),\s*Category:\s*(.*)""".toRegex()
	val configs = mutableListOf<Pair<String,String>>()
	
	fromFile.inputStream().bufferedReader().forEachLine { line ->
		val matchResult = regex.matchEntire(line) ?: return@forEachLine
		val groupValues = matchResult.groupValues
		val tag = groupValues[1]
		val categories = groupValues[2]
		configs.add(tag to categories)
	}
	toFile.writeText(configs.joinToString("\n","modifiers = {\n","\n}"){ (tag,categories)-> "    $tag = $categories" })
}
	