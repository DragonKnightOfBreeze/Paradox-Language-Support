package icu.windea.pls.config

import com.intellij.openapi.project.*
import com.jetbrains.rd.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import org.slf4j.*

class CwtConfigCache(
	val groups: Map<String, Map<String, CwtConfigFile>>,
	val declarations: Map<String, List<Map<String, Any?>>>,
	val project: Project
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
	}
	
	val resolvedGroups: Map<String, CwtConfigGroupCache>
	
	val locales: Array<ParadoxLocale>
	val localeMap: Map<String, ParadoxLocale>
	val sequentialNumbers: Array<ParadoxSequentialNumber>
	val sequentialNumberMap: Map<String, ParadoxSequentialNumber>
	val colors: Array<ParadoxColor>
	val colorMap: Map<String, ParadoxColor>
	
	val ck2 get() = getValue(ParadoxGameType.Ck2)
	val ck3 get() = getValue(ParadoxGameType.Ck3)
	val eu4 get() = getValue(ParadoxGameType.Eu4)
	val hoi4 get() = getValue(ParadoxGameType.Hoi4)
	val ir get() = getValue(ParadoxGameType.Ir)
	val stellaris get() = getValue(ParadoxGameType.Stellaris)
	val vic2 get() = getValue(ParadoxGameType.Vic2)
	
	operator fun get(key: String) = resolvedGroups.get(key)
	fun getValue(key: String) = resolvedGroups.getValue(key)
	
	operator fun get(key: ParadoxGameType) = resolvedGroups.get(key.key)
	fun getValue(key: ParadoxGameType) = resolvedGroups.getValue(key.key)
	
	init {
		logger.info("Resolve declarations...")
		
		locales = declarations.getValue("locale").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			ParadoxLocale(name, description)
		}
		localeMap = locales.associateBy { it.name }
		sequentialNumbers = declarations.getValue("sequentialNumber").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val placeholderText = it.getValue("placeholderText") as String
			ParadoxSequentialNumber(name, description, placeholderText)
		}
		sequentialNumberMap = sequentialNumbers.associateBy { it.name }
		colors = declarations.getValue("color").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val colorRgb = it.getValue("colorRgb") as Int
			val colorText = it.getValue("colorText") as String
			ParadoxColor(name, description, colorRgb, colorText)
		}
		colorMap = colors.associateBy { it.name }
		
		logger.info("Resolve declarations finished.")
		
		logger.info("Resolve config groups...")
		
		resolvedGroups = ConcurrentHashMap()
		for((groupName, group) in groups) {
			val gameType = ParadoxGameType.resolve(groupName)
			if(gameType != null) {
				resolvedGroups[groupName] = CwtConfigGroupCache(group, gameType, project)
			}
		}
		
		logger.info("Resolve config groups finished.")
	}
}