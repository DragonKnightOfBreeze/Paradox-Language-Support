package icu.windea.pls.config

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
){
	companion object{
		private val logger = LoggerFactory.getLogger(CwtConfigProvider::class.java)
		private val yaml = Yaml()
	}
	
	private val groups: MutableMap<String, Map<String, CwtConfigFile>>
	private val declarations:MutableMap<String,List<Map<String,Any?>>>
	
	internal val configGroupsCache: CwtConfigCache
	
	init {
		groups = ConcurrentHashMap<String, Map<String, CwtConfigFile>>()
		declarations = ConcurrentHashMap<String,List<Map<String,Any?>>>()
		configGroupsCache = ReadAction.compute<CwtConfigCache,Exception> {
			initConfigGroups()
			CwtConfigCache(groups,declarations,project)
		}
	}
	
	@Synchronized
	private fun initConfigGroups(){
		//TODO 尝试并发解析以提高IDE启动速度
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
					val groupName = file.name
					//defaultProject不需要解析具体的config group
					if(project == getDefaultProject()){
						this.groups[groupName] = emptyMap()
					}else {
						val group = ConcurrentHashMap<String, CwtConfigFile>()
						val groupPath = file.path
						logger.info("Init config group '$groupName'...")
						addConfigGroup(group, file, groupPath, project)
						this.groups[groupName] = group
					}
				}
				//解析顶层文件declarations.yml
				file.name == "declarations.yml" -> {
					initDeclarations(file)
				}
				//忽略其他顶层的文件
			}
		}
		logger.info("Init config groups finished.")
	}
	
	private fun addConfigGroup(group:MutableMap<String, CwtConfigFile>,parentFile: VirtualFile,groupPath:String,project: Project){
		for(file in parentFile.children) {
			//忽略扩展名不匹配的文件
			when{
				file.isDirectory -> addConfigGroup(group,file,groupPath,project)
				file.extension == "cwt" -> {
					val configName = file.path.removePrefix(groupPath)
					val config = resolveConfig(file,project)
					if(config != null){
						group[configName] = config
					}else{
						logger.warn("Cannot resolve config file '$configName', skip it.")
					}
				} 
			}
		}
	}
	
	private fun resolveConfig(file:VirtualFile,project: Project): CwtConfigFile? {
		return try {
			file.toPsiFile<CwtFile>(project)?.resolveConfig()
		} catch(e: Exception) {
			logger.warn(e.message,e)
			null
		}
	}
	
	private fun initDeclarations(file: VirtualFile) {
		logger.info("Init declarations...")
		val declarations = resolveYamlConfig(file)
		if(declarations != null) this.declarations.putAll(declarations)
		logger.info("Init declarations finished.")
	}
	
	private fun resolveYamlConfig(file:VirtualFile):Map<String,List<Map<String,Any?>>>?{
		return try {
			yaml.load(file.inputStream)
		}catch(e:Exception){
			logger.warn(e.message,e)
			null
		}
	}
}