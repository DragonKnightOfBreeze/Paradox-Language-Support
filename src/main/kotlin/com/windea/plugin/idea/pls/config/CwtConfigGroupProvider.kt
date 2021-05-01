package com.windea.plugin.idea.pls.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.psi.*
import java.util.concurrent.*

class CwtConfigGroupProvider{
	internal val configGroupsCache: CwtConfigGroupsCache
	
	init {
		configGroupsCache = ReadAction.compute<CwtConfigGroupsCache,Exception> {
			val configGroups = initConfigGroups()
			CwtConfigGroupsCache(configGroups)
		}
	}
	
	@Synchronized
	private fun initConfigGroups(): Map<String, MutableMap<String, CwtConfig>> {
		val project = ProjectManager.getInstance().defaultProject
		val configGroups = ConcurrentHashMap<String, MutableMap<String, CwtConfig>>()
		val configUrl = configPath.toUrl(locationClass)
		val configFile = VfsUtil.findFileByURL(configUrl) ?: error("Cwt config path '$configUrl' is not exist.")
		for(file in configFile.children) {
			//如果是目录则将其名字作为规则组的名字
			if(file.isDirectory){
				val groupName = file.name
				val group = ConcurrentHashMap<String, CwtConfig>()
				val groupPath = file.path
				addConfigGroup(group,file,groupPath,project)
				configGroups[groupName] = group
			}
			//忽略顶层的文件
		}
		return configGroups
	}
	
	private fun addConfigGroup(group:MutableMap<String, CwtConfig>,parentFile: VirtualFile,groupPath:String,project: Project){
		for(file in parentFile.children) {
			//忽略扩展名不匹配的文件
			when{
				file.isDirectory -> addConfigGroup(group,file,groupPath,project)
				file.extension == configFileExtension -> {
					val configName = file.path.removePrefix(groupPath)
					val config = file.toPsiFile<CwtFile>(project)?.resolveConfig()?:continue
					group[configName] = config
				} 
			}
		}
	}
}