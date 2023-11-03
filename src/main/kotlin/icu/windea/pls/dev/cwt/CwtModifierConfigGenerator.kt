package icu.windea.pls.dev.cwt

import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import java.io.*

/**
 * 用于从`modifiers.log`生成`modifiers.cwt`和`modifier_categories.cwt`
 */
class CwtModifierConfigGenerator(
	val gameType: ParadoxGameType,
	val modifiersLogPath: String,
	val modifiersCwtPath: String,
	val modifierCategoriesCwtPath: String
) {
	private val regex = when(gameType) {
		ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
		else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
	}
	private val categoryRegex = """\t(\w+|".*?")\s*=\s*\{""".toRegex()
	
	data class ModifierInfo(
		val name: String,
		val categories: Set<String>
	)
	
	fun generate() {
		setUserDir()
		
		val modifiersLogFile = File(modifiersLogPath)
		val modifiersCwtFile = File(modifiersCwtPath)
		val modifierCategoriesCwtFile = File(modifierCategoriesCwtPath)
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
		
		val missingModifierCategories = modifierCategories.toMutableSet()
		modifierCategoriesCwtFile.inputStream().bufferedReader().forEachLine { line ->
			val matchResult = categoryRegex.matchEntire(line) ?: return@forEachLine
			val groupValues = matchResult.groupValues
			val modifierCategory = groupValues[1].trim().unquote()
			missingModifierCategories.remove(modifierCategory)
		}
		if(missingModifierCategories.isNotEmpty()) {
			println()
			println("Missing modifier categories:")
			for(modifierCategory in missingModifierCategories) {
				println("- $modifierCategory")
			}
		}
		
		//TODO update modifier_categories.cwt
	}
	
	private fun setUserDir() {
		val userDir = System.getProperty("user.dir").toPath().normalize().joinToString("/").removeSuffix("/src/test")
		System.setProperty("user.dir", userDir)
	}
}