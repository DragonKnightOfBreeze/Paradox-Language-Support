package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.model.*
import java.util.concurrent.*

class CwtConfigGroupsImpl(
	override val project: Project,
	cwtFileConfigGroups: CwtConfigMaps
) : CwtConfigGroups {
	override val groups: MutableMap<String, CwtConfigGroup> = ConcurrentHashMap()
	
	init {
		//初始化各个游戏分组的CWT规则
		val coreGroupInfo = cwtFileConfigGroups.keys.first { it.groupName == "core" }
		val coreCwtFileConfigs = cwtFileConfigGroups.getValue(coreGroupInfo)
		val coreGroup = CwtConfigGroupImpl(project, null, coreGroupInfo, coreCwtFileConfigs)
		coreGroupInfo.configGroup = coreGroup
		groups["core"] = coreGroup
		for((groupInfo, cwtFileConfigs) in cwtFileConfigGroups) {
			val gameType = ParadoxGameType.resolve(groupInfo.groupName)
			if(gameType != null) {
				cwtFileConfigs.putAll(coreCwtFileConfigs)
				val group = CwtConfigGroupImpl(project, gameType, groupInfo, cwtFileConfigs)
				groupInfo.configGroup = group
				groups[groupInfo.groupName] = group
			}
		}
	}
}
