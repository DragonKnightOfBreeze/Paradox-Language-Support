package icu.windea.pls.cwt.config

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import org.slf4j.*

class CwtConfigGroups(
	val project: Project,
	declarations: Map<String, List<Map<String, Any?>>>,
	cwtFileConfigGroups: Map<String, Map<String, CwtFileConfig>>
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigGroup::class.java)
	}
	
	val groups: Map<String, CwtConfigGroup>
	
	val locales: Array<ParadoxLocaleInfo>
	val localeMap: Map<String, ParadoxLocaleInfo>
	val sequentialNumbers: Array<ParadoxSequentialNumberInfo>
	val sequentialNumberMap: Map<String, ParadoxSequentialNumberInfo>
	val colors: Array<ParadoxColorInfo>
	val colorMap: Map<String, ParadoxColorInfo>
	
	val ck2 get() = getValue(ParadoxGameType.Ck2)
	val ck3 get() = getValue(ParadoxGameType.Ck3)
	val eu4 get() = getValue(ParadoxGameType.Eu4)
	val hoi4 get() = getValue(ParadoxGameType.Hoi4)
	val ir get() = getValue(ParadoxGameType.Ir)
	val stellaris get() = getValue(ParadoxGameType.Stellaris)
	val vic2 get() = getValue(ParadoxGameType.Vic2)
	
	operator fun get(key: String) = groups.get(key)
	fun getValue(key: String) = groups.getValue(key)
	
	operator fun get(key: ParadoxGameType) = groups.get(key.key)
	fun getValue(key: ParadoxGameType) = groups.getValue(key.key)
	
	init {
		logger.info("Resolve declarations...")
		locales = declarations.getValue("locale").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			ParadoxLocaleInfo(name, description)
		}
		localeMap = locales.associateBy { it.name }
		sequentialNumbers = declarations.getValue("sequentialNumber").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val placeholderText = it.getValue("placeholderText") as String
			ParadoxSequentialNumberInfo(name, description, placeholderText)
		}
		sequentialNumberMap = sequentialNumbers.associateBy { it.name }
		colors = declarations.getValue("color").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val colorRgb = it.getValue("colorRgb") as Int
			val colorText = it.getValue("colorText") as String
			ParadoxColorInfo(name, description, colorRgb, colorText)
		}
		colorMap = colors.associateBy { it.name }
		logger.info("Resolve declarations finished.")
		
		logger.info("Resolve config groups...")
		groups = mutableMapOf()
		for((groupName, cwtFileConfigs) in cwtFileConfigGroups) {
			val gameType = ParadoxGameType.resolve(groupName)
			if(gameType != null) {
				groups[groupName] = CwtConfigGroup(gameType, project, cwtFileConfigs)
			}
		}
		logger.info("Resolve config groups finished.")
	}
}