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
 * 提供CWT规则。
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
		for(configFile in configDirectory.children) {
			if(configFile.isDirectory) {
				//将目录的名字作为规则组的名字
				resolveConfigFilesOfGroup(configMaps, configFile, configRootDirectory)
			}
		}
	}
	
	private fun resolveConfigFilesOfGroup(configMaps: CwtConfigMaps, groupDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		val groupName = getGroupName(groupDirectory)
		val groupInfo = CwtConfigGroupInfo(groupName)
		logger.info("Resolve cwt config files of group '$groupName'.")
		val configMap = configMaps.getOrPut(groupInfo) { mutableMapOf() }
		resolveConfigFilesInGroup(configMap, groupDirectory, configRootDirectory, groupInfo)
	}
	
	private fun resolveConfigFilesInGroup(configMap: CwtConfigMap, configDirectory: VirtualFile, configRootDirectory: VirtualFile, groupInfo: CwtConfigGroupInfo) {
		VfsUtilCore.visitChildrenRecursively(configDirectory, object :VirtualFileVisitor<Void>() {
			override fun visitFile(file: VirtualFile): Boolean {
				if(file.isDirectory) return true
				when(file.extension) {
					"cwt" -> resolveCwtConfigFile(configMap, file, configRootDirectory, groupInfo) //解析cwt配置文件
					else -> pass()
				}
				return true
			}
		})
	}
	
	private fun resolveCwtConfigFile(configMap: CwtConfigMap, configFile: VirtualFile, configRootDirectory: VirtualFile, groupInfo: CwtConfigGroupInfo) {
		val relativePath = configFile.relativePathTo(configRootDirectory)
		logger.info("Resolve cwt config file '$relativePath'.")
		val config = doResolveCwtConfigFile(configFile, groupInfo)
		if(config == null) {
			logger.warn("Resolve cwt config file '$relativePath' failed. Skip it.")
			return
		}
		configMap.put(relativePath, config)
	}
	
	private fun doResolveCwtConfigFile(configFile: VirtualFile, groupInfo: CwtConfigGroupInfo): CwtFileConfig? {
		return try {
			configFile.toPsiFile<CwtFile>(project)?.let { CwtConfigResolver.resolve(it, groupInfo) }
		} catch(e: Exception) {
			logger.warn(e.message, e)
			null
		}
	}
	
	private fun getGroupName(groupDirectory: VirtualFile): String {
		return groupDirectory.name
	}
}
