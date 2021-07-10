package icu.windea.pls.cwt.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import org.slf4j.*
import org.yaml.snakeyaml.*
import java.util.concurrent.*

class CwtConfigProvider(
	private val project: Project
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigProvider::class.java)
		private val yaml = Yaml()
	}
	
	private val declarationMap: MutableMap<String, List<Map<String, Any?>>>
	private val cwtFileConfigGroups: MutableMap<String, MutableMap<String, CwtFileConfig>>
	private val logFileGroups: MutableMap<String, MutableMap<String, VirtualFile>>
	private val csvFileGroups: MutableMap<String, MutableMap<String, VirtualFile>>
	
	val configGroups: CwtConfigGroups
	
	init {
		declarationMap = ConcurrentHashMap()
		cwtFileConfigGroups = ConcurrentHashMap()
		logFileGroups = ConcurrentHashMap()
		csvFileGroups = ConcurrentHashMap()
		configGroups = ReadAction.compute<CwtConfigGroups, Exception> {
			initConfigGroups()
			CwtConfigGroups(declarationMap, cwtFileConfigGroups, logFileGroups, csvFileGroups)
		}
	}
	
	@Synchronized
	private fun initConfigGroups() {
		//TODO 尝试并发解析以提高IDE启动速度
		val startTime = System.currentTimeMillis()
		logger.info("Init config groups...")
		val configUrl = "/config".toUrl(locationClass)
		//这里有可能找不到，这时不要报错，之后还会执行到这里
		//val configFile = VfsUtil.findFileByURL(configUrl) ?: error("Cwt config path '$configUrl' is not exist.")
		val configFile = VfsUtil.findFileByURL(configUrl) ?: return
		val children = configFile.children
		for(file in children) {
			when {
				//如果是目录则将其名字作为规则组的名字
				file.isDirectory -> {
					val groupName = file.name.removeSuffix("-ext") //如果有后缀"-ext"，表示这是额外提供的配置
					initConfigGroup(groupName, file)
				}
				//解析顶层文件declarations.yml
				file.name == "declarations.yml" -> {
					initDeclarations(file)
				}
				//忽略其他顶层的文件
			}
		}
		val endTime = System.currentTimeMillis()
		logger.info("Init config groups finished. (${endTime - startTime} ms)")
	}
	
	private fun initConfigGroup(groupName: String, groupFile: VirtualFile) {
		logger.info("Init config group '$groupName'...")
		addConfigGroup(groupName, groupFile, project)
		logger.info("Init config group '$groupName' finished.")
	}
	
	private fun addConfigGroup(groupName: String, parentFile: VirtualFile, project: Project) {
		//可以额外补充配置，即配置组可能不止初始化一次
		val cwtFileConfigGroup = cwtFileConfigGroups.getOrPut(groupName) { mutableMapOf() }
		val logFileGroup = logFileGroups.getOrPut(groupName) { mutableMapOf() }
		val csvFileGroup = csvFileGroups.getOrPut(groupName) { mutableMapOf() }
		
		val groupPath = parentFile.path
		val configNamePrefix = "$groupPath/"
		for(file in parentFile.children) {
			if(file.isDirectory) {
				addConfigGroup(groupName, file, project)
				return
			}
			when(file.extension) {
				"cwt" -> {
					val configName = file.path.removePrefix(configNamePrefix)
					val config = resolveConfig(file, project)
					if(config != null) {
						cwtFileConfigGroup[configName] = config
					} else {
						logger.warn("Cannot resolve config file '$configName', skip it.")
					}
				}
				"log" -> {
					val configName = file.path.removePrefix(configNamePrefix)
					logFileGroup[configName] = file
				}
				"csv" -> {
					val configName = file.path.removePrefix(configNamePrefix)
					csvFileGroup[configName] = file
				}
			}
		}
	}
	
	private fun resolveConfig(file: VirtualFile, project: Project): CwtFileConfig? {
		return try {
			file.toPsiFile<CwtFile>(project)?.resolveConfig()
		} catch(e: Exception) {
			logger.warn(e.message, e)
			null
		}
	}
	
	private fun initDeclarations(file: VirtualFile) {
		logger.info("Init declarations...")
		val declarations = resolveYamlConfig(file)
		if(declarations != null) declarationMap.putAll(declarations)
		logger.info("Init declarations finished.")
	}
	
	private fun resolveYamlConfig(file: VirtualFile): Map<String, List<Map<String, Any?>>>? {
		return try {
			yaml.load(file.inputStream)
		} catch(e: Exception) {
			logger.warn(e.message, e)
			null
		}
	}
}