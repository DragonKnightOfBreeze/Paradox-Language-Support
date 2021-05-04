package com.windea.plugin.idea.pls.config

import com.jetbrains.rd.util.*
import com.windea.plugin.idea.pls.model.*
import org.slf4j.*

class CwtConfigGroupsCache(
	val groups: Map<String, Map<String, CwtConfig>>,
	val declarations:Map<String,List<Map<String,Any?>>>
){
	companion object{
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
	}
	
	private val groupCaches:Map<ParadoxGameType,CwtConfigGroupCache>
	
	val locales:List<ParadoxLocale>
	val localeMap:Map<String,ParadoxLocale>
	val sequentialNumbers:List<ParadoxSequentialNumber>
	val sequentialNumberMap:Map<String,ParadoxSequentialNumber>
	val colors :List<ParadoxColor>
	val colorMap :Map<String,ParadoxColor>
	
	val ck2 get() = get(ParadoxGameType.Ck2)
	val ck3 get() = get(ParadoxGameType.Ck3)
	val eu4 get() = get(ParadoxGameType.Eu4)
	val hoi4 get() = get(ParadoxGameType.Hoi4)
	val ir get() = get(ParadoxGameType.Ir)
	val stellaris get() = get(ParadoxGameType.Stellaris)
	val vic2 get() = get(ParadoxGameType.Vic2)
	
	operator fun get(gameType: ParadoxGameType) = groupCaches.getValue(gameType)
	
	init {
		logger.info("Resolve declarations...")
		
		locales = declarations.getValue("locale").map {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			ParadoxLocale(name,description)
		}
		localeMap = locales.associateBy { it.name }
		sequentialNumbers = declarations.getValue("sequentialNumber").map { 
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val placeholderText = it.getValue("placeholderText") as String
			ParadoxSequentialNumber(name,description,placeholderText)
		}
		sequentialNumberMap = sequentialNumbers.associateBy { it.name }
		colors = declarations.getValue("color").map { 
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val colorRgb = it.getValue("colorRgb") as Int
			val colorText = it.getValue("colorText") as String
			ParadoxColor(name,description, colorRgb, colorText)
		}
		colorMap = colors.associateBy { it.name }
		
		logger.info("Resolve declarations finished.")
		
		logger.info("Resolve config groups...")
		
		groupCaches = ConcurrentHashMap()
		for((groupName,group) in groups) {
			val gameType = ParadoxGameType.resolve(groupName)?:continue
			groupCaches[gameType] = CwtConfigGroupCache(group,gameType,groupName)
		}
		
		logger.info("Resolve config groups finished.")
	}
}