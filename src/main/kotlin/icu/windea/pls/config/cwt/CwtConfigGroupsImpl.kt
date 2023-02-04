package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.config.cwt.config.*
import java.util.concurrent.*

class CwtConfigGroupsImpl(
	override val project: Project,
	fileGroups: MutableMap<String, MutableMap<String, VirtualFile>>
) : CwtConfigGroups {
	override val groups: MutableMap<String, CwtConfigGroup> = ConcurrentHashMap()
	
	init {
		//初始化各个游戏分组的CWT规则
		val coreFileGroup = fileGroups.getValue("core")
		val coreGroupInfo = CwtConfigGroupInfo("root")
		val coreConfigGroup = CwtConfigGroupImpl(project, null, coreGroupInfo, coreFileGroup)
		coreGroupInfo.configGroup = coreConfigGroup
		groups["core"] = coreConfigGroup
		for((groupName, fileGroup) in fileGroups) {
			val gameType = ParadoxGameType.resolve(groupName)
			if(gameType != null) {
				fileGroup.putAll(coreFileGroup)
				val configInfo = CwtConfigGroupInfo(groupName)
				val configGroup = CwtConfigGroupImpl(project, gameType, configInfo, fileGroup)
				configInfo.configGroup = configGroup
				groups[groupName] = configGroup
			}
		}
	}
}
