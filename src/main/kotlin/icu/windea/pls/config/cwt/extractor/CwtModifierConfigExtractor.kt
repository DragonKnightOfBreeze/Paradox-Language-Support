package icu.windea.pls.config.cwt.extractor

import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import java.io.*

/**
 * 用于从`logs/script_documentation/modifiers.log`生成`modifiers.cwt`。
 */
object CwtModifierConfigExtractor {
	data class ModifierConfig(
		val name: String,
		val categories: Set<String>
	)
	
	@JvmStatic
	fun main(args: Array<String>) {
		extract(
			"cwt/cwtools-stellaris-config/script-docs/modifiers.log",
			"cwt/cwtools-stellaris-config/config/modifiers.pls.cwt"
		)
	}
	
	fun extract(fromPath: String, toPath: String) {
		val fromFile = File(fromPath)
		val toFile = File(toPath)
		val regex = """- (.*),\s*Category:\s*(.*)""".toRegex()
		val configs = mutableListOf<ModifierConfig>()
		fromFile.inputStream().bufferedReader().forEachLine { line ->
			val matchResult = regex.matchEntire(line) ?: return@forEachLine
			val groupValues = matchResult.groupValues
			val modifier = groupValues[1]
			val categories = groupValues[2].splitToSequence(',').mapTo(mutableSetOf()) { it.trim() }
			val config = ModifierConfig(modifier, categories)
			configs.add(config)
		}
		val text = buildString {
			append("modifiers = {").appendLine()
			configs.forEach { (name, categories) ->
				append(name).append(" = {")
				categories.forEach { category -> append(" ").append(category.quote()) }
				append(" }").appendLine()
			}
			appendLine("}")
		}
		toFile.writeText(text)
	}
}