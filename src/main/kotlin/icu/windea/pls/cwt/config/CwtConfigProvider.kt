package icu.windea.pls.cwt.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import org.slf4j.*
import org.yaml.snakeyaml.*
import kotlin.system.*

class CwtConfigProvider(
	val project: Project
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigProvider::class.java)
		private val yaml = Yaml()
	}
	
	private val declarationMap: MutableMap<String, List<Map<String, Any?>>> = mutableMapOf()
	private val cwtFileConfigGroups: MutableMap<String, MutableMap<String, CwtFileConfig>> = mutableMapOf()
	private val logFileGroups: MutableMap<String, MutableMap<String, VirtualFile>> = mutableMapOf()
	private val csvFileGroups: MutableMap<String, MutableMap<String, VirtualFile>> = mutableMapOf()
	
	val configGroups: CwtConfigGroups = ReadAction.compute<CwtConfigGroups,Exception> {  initConfigGroups() }
	
	//执行时间：15724ms（读取15446ms，解析278ms）
	//优化后的执行时间：ms（读取ms，解析ms） 
	@Synchronized
	private fun initConfigGroups(): CwtConfigGroups {
		logger.info("Init config groups...")
		val startTime = System.currentTimeMillis()
		val configUrl = "/config".toUrl(locationClass)
		//这里有可能找不到，这时不要报错，之后还会执行到这里
		val configFile = VfsUtil.findFileByURL(configUrl)
		if(configFile != null) {
			//NOTE 并发迭代
			val children = configFile.children
			for(file in children) {
				val fileName = file.name
				when {
					//如果是目录则将其d名字作为规则组的名字
					file.isDirectory -> {
						val groupName = fileName.removeSuffix("-ext") //如果有后缀"-ext"，表示这是额外提供的配置
						initConfigGroup(groupName, file)
					}
					//解析顶层文件declarations.yml
					fileName == "declarations.yml" -> {
						initDeclarations(file)
					}
					//忽略其他顶层的文件
				}
			}
		}
		val readTime = System.currentTimeMillis() - startTime
		val configGroups = CwtConfigGroups(project, declarationMap, cwtFileConfigGroups, logFileGroups, csvFileGroups)
		val time = System.currentTimeMillis() - startTime
		logger.info("Init config groups finished. ($time ms)") 
		return configGroups
	}
	
	private fun initConfigGroup(groupName: String, groupFile: VirtualFile) {
		logger.info("Init config group '$groupName'...")
		addConfigGroup(groupName, groupFile, "${groupFile.path}/", project)
		logger.info("Init config group '$groupName' finished.")
	}
	
	private fun addConfigGroup(groupName: String, groupFile: VirtualFile, pathPrefix: String, project: Project) {
		//可以额外补充配置，即配置组可能不止初始化一次
		val cwtFileConfigGroup = cwtFileConfigGroups.getOrPut(groupName) { mutableMapOf() }
		val logFileGroup = logFileGroups.getOrPut(groupName) { mutableMapOf() }
		val csvFileGroup = csvFileGroups.getOrPut(groupName) { mutableMapOf() }
		
		for(file in groupFile.children) {
			if(file.isDirectory) {
				addConfigGroup(groupName, file, pathPrefix, project)
				continue
			}
			when(file.extension) {
				"cwt" -> {
					val configName = file.path.removePrefix(pathPrefix)
					val config = resolveConfig(file, project)
					if(config != null) {
						cwtFileConfigGroup[configName] = config
					} else {
						logger.warn("Cannot resolve config file '$configName', skip it.")
					}
				}
				"log" -> {
					val configName = file.path.removePrefix(pathPrefix)
					logFileGroup[configName] = file
				}
				"csv" -> {
					val configName = file.path.removePrefix(pathPrefix)
					csvFileGroup[configName] = file
				}
			}
		}
	}
	
	private fun resolveConfig(file: VirtualFile, project: Project): CwtFileConfig? {
		return try {
			file.toPsiFile<CwtFile>(project)?.resolveConfig()
			//val result: CwtFileConfig?
			//measureTimeMillis {
			//	result = file.toPsiFile<CwtFile>(project)?.resolveConfig()
			//}.also { 
			//	println("${file.name}: $it") 
			//}
			//result
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
	
	//执行时间：134ms
	private fun resolveYamlConfig(file: VirtualFile): Map<String, List<Map<String, Any?>>>? {
		return try {
			yaml.load(file.inputStream)
			//val result: Map<String, List<Map<String, Any?>>>?
			//measureTimeMillis {
			//	result = yaml.load(file.inputStream)
			//}.also { 
			//	println("${file.name}: $it")
			//}
			//result
		} catch(e: Exception) {
			logger.warn(e.message, e)
			null
		}
	}
}