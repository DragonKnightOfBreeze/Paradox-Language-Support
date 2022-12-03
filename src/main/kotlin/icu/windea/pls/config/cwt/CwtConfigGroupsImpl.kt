package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.model.*
import java.util.concurrent.*

class CwtConfigGroupsImpl(
	override val project: Project,
	cwtFileConfigGroups: MutableMap<String, MutableMap<String, CwtFileConfig>>
) : CwtConfigGroups {
	override val groups: MutableMap<String, CwtConfigGroup> = ConcurrentHashMap()
	
	init {
		//初始化各个游戏分组的CWT规则
		val coreCwtFileConfigs = cwtFileConfigGroups.getValue("core")
		groups["core"] = CwtConfigGroupImpl(project, null, coreCwtFileConfigs)
		for((groupName, cwtFileConfigs) in cwtFileConfigGroups) {
			val gameType = ParadoxGameType.resolve(groupName)
			if(gameType != null) {
				cwtFileConfigs.putAll(coreCwtFileConfigs)
				groups[groupName] = CwtConfigGroupImpl(project, gameType, cwtFileConfigs)
			}
		}
	}
}
