package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.model.*

@Suppress("unused")
class CwtConfigGroups(
	project: Project,
	cwtFileConfigGroups: Map<String, Map<String, CwtFileConfig>>
) {
	val groups: Map<String, CwtConfigGroup>
	
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
		//初始化各个游戏分组的CWT规则
		groups = mutableMapOf()
		for((groupName, cwtFileConfigs) in cwtFileConfigGroups) {
			val gameType = ParadoxGameType.resolve(groupName)
			if(gameType != null) {
				groups[groupName] = CwtConfigGroup(gameType, project, cwtFileConfigs)
			}
		}
	}
}