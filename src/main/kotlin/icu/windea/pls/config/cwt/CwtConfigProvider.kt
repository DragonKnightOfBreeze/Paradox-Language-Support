package icu.windea.pls.config.cwt

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import java.lang.invoke.*

/**
 * CWT规则的提供器。
 *
 * CWT规则来自目录`config/cwt`中的配置文件。使用内置且经过扩展和修改的CWT配置文件。
 */
@Service(Service.Level.PROJECT)
class CwtConfigProvider(
	private val project: Project
) {
	companion object {
		private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
		
		private const val cwtConfigPath = "/config/cwt"
	}
	
	val configGroups: CwtConfigGroups = initConfigGroups()
	
	//执行时间：读取3698ms 解析66ms
	@Synchronized
	private fun initConfigGroups(): CwtConfigGroups {
		val configMaps = initConfigMaps()
		val startTime = System.currentTimeMillis()
		logger.info("Init cwt config groups.")
		val configGroups = CwtConfigGroupsImpl(project, configMaps)
		val endTime = System.currentTimeMillis()
		logger.info("Init config groups finished. (${endTime - startTime} ms)")
		return configGroups
	}
	
	private fun initConfigMaps(): CwtConfigMaps {
		val startTime = System.currentTimeMillis()
		logger.info("Resolve cwt config files.")
		val configMaps: CwtConfigMaps = mutableMapOf()
		val configUrl = cwtConfigPath.toClasspathUrl()
		//通过这种方式得到的virtualFile可以位于jar压缩包中，可以直接得到它的子节点
		val configDirectory = VfsUtil.findFileByURL(configUrl)
		if(configDirectory != null) {
			//这里必须使用ReadAction
			ReadAction.run<Exception> {
				resolveConfigFiles(configMaps, configDirectory, configDirectory)
			}
		}
		val endTime = System.currentTimeMillis()
		logger.info("Resolve cwt config files finished. (${endTime - startTime} ms)")
		return configMaps
	}
	
	private fun resolveConfigFiles(configMaps: CwtConfigMaps, configDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		val globalConfigMap: CwtConfigMap = mutableMapOf()
		for(configFile in configDirectory.children) {
			if(configFile.isDirectory) {
				//将目录的名字作为规则组的名字
				resolveConfigFilesOfGroup(configMaps, configFile, configRootDirectory)
			} else {
				when(configFile.extension) {
					"cwt" -> resolveGlobalCwtConfigFile(globalConfigMap, configFile, configRootDirectory) //解析共享的cwt配置文件
					else -> pass() //不做处理
				}
			}
		}
		//共享的配置会覆盖特定游戏类型的配置
		for(configMap in configMaps.values) {
			configMap.putAll(globalConfigMap)
		}
	}
	
	private fun resolveConfigFilesOfGroup(configMaps: CwtConfigMaps, groupDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		val groupName = getGroupName(groupDirectory)
		logger.info("Resolve cwt config files of group '$groupName'.")
		val configMap = configMaps.getOrPut(groupName) { mutableMapOf() }
		resolveConfigFilesInGroup(configMap, groupDirectory, groupDirectory, configRootDirectory)
	}
	
	private fun resolveConfigFilesInGroup(configMap: CwtConfigMap, configDirectory: VirtualFile, groupDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		for(configFile in configDirectory.children) {
			if(configFile.isDirectory) {
				//继续解析子目录里面的配置文件
				resolveConfigFilesInGroup(configMap, configFile, groupDirectory, configRootDirectory)
			} else {
				when(configFile.extension) {
					"cwt" -> resolveCwtConfigFile(configMap, configFile, groupDirectory, configRootDirectory) //解析cwt配置文件
					else -> pass() //不做处理
				}
			}
		}
	}
	
	private fun resolveGlobalCwtConfigFile(globalConfigMap: CwtConfigMap, configFile: VirtualFile, configRootDirectory: VirtualFile) {
		val relativePath = configFile.relativePathTo(configRootDirectory)
		logger.info("Resolve global cwt config file '$relativePath'.")
		val config = doResolveCwtConfigFile(configFile)
		if(config == null) {
			logger.warn("Resolve global cwt config file '$relativePath' failed. Skip it.")
			return
		}
		val configName = "~$relativePath" //cwt文件名应该不会以"~"开头吧
		globalConfigMap.put(configName, config)
	}
	
	private fun resolveCwtConfigFile(configMap: CwtConfigMap, configFile: VirtualFile, groupDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		val relativePath = configFile.relativePathTo(configRootDirectory)
		logger.info("Resolve cwt config file '$relativePath'.")
		val config = doResolveCwtConfigFile(configFile)
		if(config == null) {
			logger.warn("Resolve cwt config file '$relativePath' failed. Skip it.")
			return
		}
		val configName = configFile.relativePathTo(groupDirectory)
		configMap.put(configName, config)
	}
	
	private fun doResolveCwtConfigFile(configFile: VirtualFile): CwtFileConfig? {
		return try {
			configFile.toPsiFile<CwtFile>(project)?.let { CwtConfigResolver.resolve(it) }
		} catch(e: Exception) {
			logger.warn(e.message, e)
			null
		}
	}
	
	private fun getGroupName(groupDirectory: VirtualFile): String {
		return groupDirectory.name
	}
}
