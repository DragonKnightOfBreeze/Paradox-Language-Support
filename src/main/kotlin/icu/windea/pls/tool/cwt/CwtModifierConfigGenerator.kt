package icu.windea.pls.tool.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import java.io.*

/**
 * 用于从`modifiers.log`生成`modifiers.cwt`和`modifier_categories.cwt`
 */
class CwtModifierConfigGenerator(
	val project: Project,
	val gameType: ParadoxGameType,
	val modifiersLogPath: String,
	val modifiersCwtPath: String,
	val modifierCategoriesPath: String
) {
	data class ModifierInfo(
		val name: String,
		val categories: Set<String>
	)
	
	fun generate() {
		val modifiersLogFile = File(modifiersLogPath)
		val modifiersCwtFile = File(modifiersCwtPath)
		val modifierCategoriesFile = File(modifierCategoriesPath)
		val regex = when(gameType) {
			ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
			else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
		}
		val infos = mutableListOf<ModifierInfo>()
		modifiersLogFile.inputStream().bufferedReader().forEachLine { line ->
			val matchResult = regex.matchEntire(line) ?: return@forEachLine
			val groupValues = matchResult.groupValues
			val modifier = groupValues[1].trim()
			val categories = groupValues[2].splitToSequence(',').mapTo(mutableSetOf()) { it.trim() }
			val info = ModifierInfo(modifier, categories)
			infos.add(info)
		}
		val text = buildString {
			append("modifiers = {").appendLine()
			val categorySize = infos.maxOfOrNull { it.categories.size } ?: 0
			infos.forEach { (name, categories) ->
				append("\t")
				append(name)
				append(" = ")
				if(categorySize == 1) {
					append(categories.single().quoteIfNecessary())
				} else {
					append("{")
					categories.forEach { category -> append(" ").append(category.quoteIfNecessary()) }
					append(" }")
				}
				appendLine()
			}
			appendLine("}")
		}
		modifiersCwtFile.writeText(text)
		
		val modifierCategories = infos.flatMapTo(mutableSetOf()) { it.categories }
		println("Modifier categories:")
		for(modifierCategory in modifierCategories) {
			println("- $modifierCategory")
		}
	}
}