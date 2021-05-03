package com.windea.plugin.idea.pls.config

import com.jetbrains.rd.util.*
import com.windea.plugin.idea.pls.model.*
import org.slf4j.*

class CwtConfigGroupsCache(val configGroups: Map<String, Map<String, CwtConfig>>){
	companion object{
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
	}
	
	private val configGroupCaches:Map<ParadoxGameType,CwtConfigGroupCache>
	
	operator fun get(gameType: ParadoxGameType) = configGroupCaches.getValue(gameType)
	
	val ck2 get() = get(ParadoxGameType.Ck2)
	val ck3 get() = get(ParadoxGameType.Ck3)
	val eu4 get() = get(ParadoxGameType.Eu4)
	val hoi4 get() = get(ParadoxGameType.Hoi4)
	val ir get() = get(ParadoxGameType.Ir)
	val stellaris get() = get(ParadoxGameType.Stellaris)
	val vic2 get() = get(ParadoxGameType.Vic2)
	
	init {
		logger.info("Resolve config groups...")
		
		configGroupCaches = ConcurrentHashMap()
		for((groupName,group) in configGroups) {
			val gameType = ParadoxGameType.resolve(groupName)?:continue
			configGroupCaches[gameType] = CwtConfigGroupCache(group,gameType,groupName)
		}
		
		logger.info("Resolve config groups finished.")
	}
}