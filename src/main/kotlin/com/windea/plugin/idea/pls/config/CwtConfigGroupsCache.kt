package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.model.*
import org.slf4j.*

class CwtConfigGroupsCache(val configGroups: Map<String, Map<String, CwtConfig>>){
	companion object{
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
	}
	
	val ck2: CwtConfigGroupCache
	val ck3: CwtConfigGroupCache
	val eu4: CwtConfigGroupCache 
	val hoi4: CwtConfigGroupCache
	val ir: CwtConfigGroupCache
	val stellaris: CwtConfigGroupCache
	val vic2: CwtConfigGroupCache
	
	init {
		logger.info("Resolve config groups...")
		
		ck2 = getConfigGroup(ParadoxGameType.Ck2)
		ck3 = getConfigGroup(ParadoxGameType.Ck3)
		eu4= getConfigGroup(ParadoxGameType.Eu4)
		hoi4 = getConfigGroup(ParadoxGameType.Hoi4)
		ir = getConfigGroup(ParadoxGameType.Ir)
		stellaris = getConfigGroup(ParadoxGameType.Stellaris)
		vic2 = getConfigGroup(ParadoxGameType.Vic2)
		
		logger.info("Resolve config groups finished.")
	}
	
	private fun getConfigGroup(gameType: ParadoxGameType): CwtConfigGroupCache {
		return CwtConfigGroupCache(configGroups.getOrDefault(gameType.key, emptyMap()),gameType,gameType.key)
	}
}